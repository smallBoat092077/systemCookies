package com.example.systemcookies.service.impl;

import com.example.systemcookies.common.CacheSupport;
import com.example.systemcookies.common.PageResult;
import com.example.systemcookies.dto.AssistantChatDTO;
import com.example.systemcookies.dto.CartAddDTO;
import com.example.systemcookies.dto.OrderCreateDTO;
import com.example.systemcookies.dto.RecipeQueryDTO;
import com.example.systemcookies.service.AssistantService;
import com.example.systemcookies.service.CartService;
import com.example.systemcookies.service.OrderService;
import com.example.systemcookies.service.RecipeService;
import com.example.systemcookies.vo.AssistantReplyVO;
import com.example.systemcookies.vo.CartVO;
import com.example.systemcookies.vo.OrderVO;
import com.example.systemcookies.vo.RecipeListVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.systemcookies.common.RedisKeys;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 点菜小助理实现：
 * - 配置了阿里云百炼（DashScope）API Key 时，走通义千问 + function calling，由后端执行工具
 * - 未配置时自动降级到本地规则引擎，同样能走完「推荐 → 加购 → 下单」全流程
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssistantServiceImpl implements AssistantService {

    private static final int MAX_TOOL_ROUNDS = 6;
    private static final int MAX_HISTORY = 20;
    private static final Duration SESSION_TTL = Duration.ofMinutes(30);

    /** 调用大模型的连接超时 */
    private static final Duration AI_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    /** 单次大模型调用的读超时，超时后由上层降级到本地规则引擎 */
    private static final Duration AI_READ_TIMEOUT = Duration.ofSeconds(25);
    /** 整轮对话留给大模型的时间预算，超出后不再调用工具，直接基于已有结果收尾 */
    private static final Duration AI_TIME_BUDGET = Duration.ofSeconds(35);

    private static final Pattern PHONE_PATTERN = Pattern.compile("(1[3-9]\\d{9})");
    private static final Pattern QUANTITY_PATTERN = Pattern.compile("(\\d+)\\s*(份|个|盘|碗|道)");

    private static final String SYSTEM_PROMPT = """
            你是「点菜小助理」，服务于一家中式菜谱点菜小程序，帮用户用对话完成点餐。
            你可以调用这些工具：search_recipes 查菜谱、add_to_cart 加入购物车、view_cart 查看购物车、submit_order 下单。
            规则：
            1. 用户说想吃什么，先用 search_recipes 找到菜品，再 add_to_cart。工具返回的「均在售」列表就是店里真实
               可点的菜，用户点的菜名只要出现在这个列表里，就直接用它，绝不要说「没有这道菜 / 没备货」，
               也不要自己编造菜品和价格。
            2. 用户说「下单 / 结算 / 就这些 / 买单」时：如果已经知道联系人姓名和手机号，立即调用 submit_order，
               不要再次确认；缺少哪一项就只问缺少的那一项，不要重复索要已有信息。
            3. 用户补上姓名和手机号（例如「张三，13800000001」）即视为确认下单，立即调用 submit_order。
            4. 回复用中文，简洁友好，单条不超过 80 字，不要用 Markdown。
            5. 能一次做完的事就一次做完，不要用「确认后我就提交」这类话把球踢回给用户。
            """;

    /** 工具定义：OpenAI 兼容格式的 function calling */
    private static final String TOOLS_JSON = """
            [
              {
                "type": "function",
                "function": {
                  "name": "search_recipes",
                  "description": "按关键字搜索菜谱，或获取招牌推荐菜",
                  "parameters": {
                    "type": "object",
                    "properties": {
                      "keyword": {"type": "string", "description": "菜名关键字，可为空表示不限"},
                      "recommendOnly": {"type": "boolean", "description": "是否只要招牌推荐"}
                    }
                  }
                }
              },
              {
                "type": "function",
                "function": {
                  "name": "add_to_cart",
                  "description": "把指定菜品加入购物车",
                  "parameters": {
                    "type": "object",
                    "properties": {
                      "recipeName": {"type": "string", "description": "菜名，支持模糊匹配"},
                      "quantity": {"type": "integer", "description": "份数，默认 1"}
                    },
                    "required": ["recipeName"]
                  }
                }
              },
              {
                "type": "function",
                "function": {
                  "name": "view_cart",
                  "description": "查看当前购物车内容",
                  "parameters": {"type": "object", "properties": {}}
                }
              },
              {
                "type": "function",
                "function": {
                  "name": "submit_order",
                  "description": "用购物车里的菜品下单，需要联系人与手机号",
                  "parameters": {
                    "type": "object",
                    "properties": {
                      "contactName": {"type": "string"},
                      "contactPhone": {"type": "string"},
                      "remark": {"type": "string", "description": "口味、忌口等备注"}
                    },
                    "required": ["contactName", "contactPhone"]
                  }
                }
              }
            ]
            """;

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.model:qwen-plus}")
    private String model;

    @Value("${app.ai.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String baseUrl;

    private final RecipeService recipeService;
    private final CartService cartService;
    private final OrderService orderService;
    private final CacheSupport cacheSupport;
    private final ObjectMapper objectMapper;
    private final RestClient.Builder restClientBuilder;

    /** 大模型客户端，首次使用时初始化 */
    private volatile RestClient aiClient;

    @Override
    public AssistantReplyVO chat(Long userId, AssistantChatDTO dto) {
        String sessionId = StringUtils.hasText(dto.getSessionId())
                ? dto.getSessionId()
                : UUID.randomUUID().toString();
        String cacheKey = RedisKeys.assistantSession(userId, sessionId);

        List<ChatMessage> history = loadHistory(cacheKey);
        history.add(new ChatMessage("user", dto.getMessage()));

        AssistantReplyVO reply;
        if (StringUtils.hasText(apiKey)) {
            try {
                reply = chatWithAi(userId, history);
                reply.setMode("ai");
            } catch (Exception e) {
                log.warn("AI 对话失败，降级本地规则引擎：{}", e.getMessage());
                reply = chatLocal(userId, dto.getMessage());
            }
        } else {
            reply = chatLocal(userId, dto.getMessage());
        }

        history.add(new ChatMessage("assistant", reply.getReply()));
        if (history.size() > MAX_HISTORY) {
            history = new ArrayList<>(history.subList(history.size() - MAX_HISTORY, history.size()));
        }
        cacheSupport.set(cacheKey, history, SESSION_TTL);

        reply.setSessionId(sessionId);
        return reply;
    }

    @Override
    public void clearSession(Long userId, String sessionId) {
        if (StringUtils.hasText(sessionId)) {
            cacheSupport.delete(RedisKeys.assistantSession(userId, sessionId));
        }
    }

    // ===================== 阿里云通义千问（DashScope 兼容 OpenAI 接口） =====================

    private AssistantReplyVO chatWithAi(Long userId, List<ChatMessage> history) {
        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> system = new LinkedHashMap<>();
        system.put("role", "system");
        system.put("content", SYSTEM_PROMPT);
        messages.add(system);
        for (ChatMessage item : history) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("role", item.getRole());
            node.put("content", item.getContent());
            messages.add(node);
        }

        List<AssistantReplyVO.ActionVO> actions = new ArrayList<>();
        List<Map<String, Object>> tools = readTools();
        long budgetNanos = AI_TIME_BUDGET.toNanos();
        long deadline = System.nanoTime() + budgetNanos;

        for (int round = 0; round < MAX_TOOL_ROUNDS && System.nanoTime() < deadline; round++) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", model);
            body.put("messages", messages);
            body.put("tools", tools);
            body.put("tool_choice", "auto");
            body.put("temperature", 0.3);

            JsonNode response = postChat(body);
            JsonNode message = response.path("choices").path(0).path("message");
            JsonNode toolCalls = message.path("tool_calls");

            if (toolCalls.isArray() && !toolCalls.isEmpty()) {
                messages.add(objectMapper.convertValue(message, Map.class));
                for (JsonNode call : toolCalls) {
                    String name = call.path("function").path("name").asText();
                    String arguments = call.path("function").path("arguments").asText("{}");
                    String result = executeTool(userId, name, arguments, actions);
                    Map<String, Object> toolMsg = new LinkedHashMap<>();
                    toolMsg.put("role", "tool");
                    toolMsg.put("tool_call_id", call.path("id").asText());
                    toolMsg.put("content", result);
                    messages.add(toolMsg);
                }
                continue;
            }

            String content = message.path("content").asText("");
            if (!StringUtils.hasText(content)) {
                content = "我没太理解，想吃点什么？可以看看招牌推荐哦。";
            }
            return buildReply(content, "ai", actions, List.of("今天有什么推荐", "看看购物车", "下单"));
        }

        // 轮次用尽或超过时间预算：不让用户空手而归，要求模型基于已拿到的工具结果直接作答
        Map<String, Object> wrapUp = new LinkedHashMap<>();
        wrapUp.put("role", "user");
        wrapUp.put("content", "请直接根据上面工具返回的结果回复用户，不要再调用工具。");
        messages.add(wrapUp);

        Map<String, Object> finalBody = new LinkedHashMap<>();
        finalBody.put("model", model);
        finalBody.put("messages", messages);
        finalBody.put("temperature", 0.3);

        String content = postChat(finalBody)
                .path("choices").path(0).path("message").path("content").asText("");
        if (!StringUtils.hasText(content)) {
            content = "我这边卡了一下，想先点哪道菜？";
        }
        return buildReply(content, "ai", actions, List.of("今天有什么推荐", "看看购物车"));
    }

    private JsonNode postChat(Map<String, Object> body) {
        String json = aiClient()
                .post()
                .uri(baseUrl + "/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);
        try {
            return objectMapper.readTree(json == null ? "{}" : json);
        } catch (Exception e) {
            log.warn("解析大模型响应失败", e);
            return objectMapper.createObjectNode();
        }
    }

    /**
     * AI 客户端：必须显式设置连接/读取超时，
     * 否则大模型侧无响应时请求会一直挂住，前端只能等到超时报错
     */
    private RestClient aiClient() {
        RestClient client = this.aiClient;
        if (client == null) {
            synchronized (this) {
                if (this.aiClient == null) {
                    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                    factory.setConnectTimeout((int) AI_CONNECT_TIMEOUT.toMillis());
                    factory.setReadTimeout((int) AI_READ_TIMEOUT.toMillis());
                    this.aiClient = restClientBuilder.requestFactory(factory).build();
                }
                client = this.aiClient;
            }
        }
        return client;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> readTools() {
        try {
            return objectMapper.readValue(TOOLS_JSON, List.class);
        } catch (Exception e) {
            log.warn("工具定义解析失败", e);
            return List.of();
        }
    }

    /** 执行工具，返回给模型的文本结果，同时记录动作供前端展示 */
    private String executeTool(
            Long userId, String name, String arguments, List<AssistantReplyVO.ActionVO> actions) {
        try {
            JsonNode args = objectMapper.readTree(arguments);
            return switch (name) {
                case "search_recipes" -> toolSearchRecipes(args, actions);
                case "add_to_cart" -> toolAddToCart(userId, args, actions);
                case "view_cart" -> toolViewCart(userId, actions);
                case "submit_order" -> toolSubmitOrder(userId, args, actions);
                default -> "不支持的工具：" + name;
            };
        } catch (Exception e) {
            log.warn("工具执行失败 {} {}", name, e.getMessage());
            return "执行失败：" + e.getMessage();
        }
    }

    private String toolSearchRecipes(JsonNode args, List<AssistantReplyVO.ActionVO> actions) {
        String keyword = args.path("keyword").asText("").trim();
        boolean recommendOnly = args.path("recommendOnly").asBoolean(false);
        List<RecipeListVO> list = recommendOnly
                ? recipeService.recommendList(5)
                : queryRecipes(keyword.isEmpty() ? null : keyword, 8);
        if (list.isEmpty()) {
            return "没有找到相关菜品。";
        }
        actions.add(action("search", "查询了菜品：" + keyword, null));
        String menu = list.stream()
                .map(item -> String.format("菜名：%s，价格：¥%s，已售 %d 份，简介：%s",
                        item.getName(),
                        item.getPrice(),
                        item.getSales() == null ? 0 : item.getSales(),
                        StringUtils.hasText(item.getSubtitle()) ? item.getSubtitle() : "招牌好菜"))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
        return "以下菜品均在售，可直接加入购物车：\n" + menu;
    }

    private String toolAddToCart(Long userId, JsonNode args, List<AssistantReplyVO.ActionVO> actions) {
        String recipeName = args.path("recipeName").asText("").trim();
        int quantity = Math.max(1, args.path("quantity").asInt(1));
        RecipeListVO recipe = matchRecipe(recipeName);
        if (recipe == null) {
            return "没有这道菜：" + recipeName + "，请换一个菜名或先看推荐。";
        }
        CartAddDTO dto = new CartAddDTO();
        dto.setUserId(userId);
        dto.setRecipeId(recipe.getId());
        dto.setQuantity(quantity);
        cartService.addItem(dto);
        actions.add(action("addCart", "已加入购物车：" + recipe.getName() + " × " + quantity, null));
        return String.format("已加入购物车：%s × %d，单价 ¥%s", recipe.getName(), quantity, recipe.getPrice());
    }

    private String toolViewCart(Long userId, List<AssistantReplyVO.ActionVO> actions) {
        List<CartVO> cart = cartService.listCart(userId);
        if (cart.isEmpty()) {
            return "购物车还是空的。";
        }
        actions.add(action("viewCart", "查看了购物车", null));
        return cart.stream()
                .map(item -> String.format("%s × %d 小计 ¥%s",
                        item.getName(), item.getQuantity(), item.getSubtotal()))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
    }

    private String toolSubmitOrder(Long userId, JsonNode args, List<AssistantReplyVO.ActionVO> actions) {
        String contactName = args.path("contactName").asText("").trim();
        String contactPhone = args.path("contactPhone").asText("").trim();
        if (!StringUtils.hasText(contactName) || !StringUtils.hasText(contactPhone)) {
            return "缺少联系人或手机号，请先向用户询问。";
        }
        List<CartVO> cart = cartService.listCart(userId);
        if (cart.isEmpty()) {
            return "购物车是空的，先加菜再下单。";
        }
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setUserId(userId);
        dto.setCartItemIds(cart.stream().map(CartVO::getId).toList());
        dto.setContactName(contactName);
        dto.setContactPhone(contactPhone);
        dto.setRemark(args.path("remark").asText("").trim());
        Long orderId = orderService.createOrder(dto);
        OrderVO order = orderService.getDetail(orderId, userId);
        actions.add(action("submitOrder", "下单成功", order.getOrderNo()));
        return "下单成功，订单号：" + order.getOrderNo();
    }

    // ===================== 本地规则兜底 =====================

    private AssistantReplyVO chatLocal(Long userId, String message) {
        String text = message == null ? "" : message.trim();
        List<AssistantReplyVO.ActionVO> actions = new ArrayList<>();

        if (text.contains("清空") || text.contains("重新开始") || text.contains("重来")) {
            cartService.clear(userId);
            return buildReply("好的，购物车已清空，我们重新点菜吧～", "local", actions,
                    List.of("今天有什么推荐", "来一份红烧肉"));
        }

        if (text.contains("购物车") && !text.contains("加")) {
            List<CartVO> cart = cartService.listCart(userId);
            if (cart.isEmpty()) {
                return buildReply("购物车还是空的，想吃点什么？", "local", actions,
                        List.of("今天有什么推荐", "来一份红烧肉"));
            }
            actions.add(action("viewCart", "查看了购物车", null));
            String detail = cart.stream()
                    .map(item -> item.getName() + " × " + item.getQuantity())
                    .reduce((a, b) -> a + "，" + b)
                    .orElse("");
            double total = cart.stream().mapToDouble(item -> item.getSubtotal().doubleValue()).sum();
            return buildReply("购物车里有：" + detail + "，合计 ¥" + String.format("%.2f", total) + "。要下单吗？",
                    "local", actions, List.of("下单", "再来一份"));
        }

        if (text.contains("下单") || text.contains("结算") || text.contains("买单")
                || text.contains("付款") || text.contains("就这些") || text.contains("好了")) {
            List<CartVO> cart = cartService.listCart(userId);
            if (cart.isEmpty()) {
                return buildReply("购物车还是空的，先选几道菜吧～", "local", actions,
                        List.of("今天有什么推荐", "来一份红烧肉"));
            }
            String phone = findPhone(text);
            if (phone == null) {
                return buildReply("马上帮你下单，请告诉我联系人手机号（下单用）。", "local", actions, List.of());
            }
            OrderCreateDTO dto = new OrderCreateDTO();
            dto.setUserId(userId);
            dto.setCartItemIds(cart.stream().map(CartVO::getId).toList());
            dto.setContactName("点菜小助理代下单");
            dto.setContactPhone(phone);
            Long orderId = orderService.createOrder(dto);
            OrderVO order = orderService.getDetail(orderId, userId);
            actions.add(action("submitOrder", "下单成功", order.getOrderNo()));
            return buildReply("下单成功啦，订单号 " + order.getOrderNo() + "，厨房马上安排～",
                    "local", actions, List.of("查看我的订单"));
        }

        // 加菜：命中菜名就加购；问价/问做法时改为介绍
        RecipeListVO hit = matchRecipe(text);
        boolean asking = text.contains("怎么") || text.contains("如何") || text.contains("做法")
                || text.contains("多少钱") || text.contains("价格") || text.contains("好吃吗");
        if (hit != null && asking) {
            String intro = StringUtils.hasText(hit.getSubtitle()) ? hit.getSubtitle() : hit.getDescription();
            return buildReply("「" + hit.getName() + "」¥" + hit.getPrice()
                            + (StringUtils.hasText(intro) ? "，" + intro : "") + "。要来一份吗？",
                    "local", actions, List.of("来一份" + hit.getName(), "看看购物车"));
        }
        if (hit != null) {
            int quantity = parseQuantity(text);
            CartAddDTO dto = new CartAddDTO();
            dto.setUserId(userId);
            dto.setRecipeId(hit.getId());
            dto.setQuantity(quantity);
            cartService.addItem(dto);
            actions.add(action("addCart", "已加入购物车：" + hit.getName() + " × " + quantity, null));
            return buildReply("已帮你加入「" + hit.getName() + "」× " + quantity + "，还要别的吗？",
                    "local", actions, List.of("看看购物车", "下单", "再来一道"));
        }

        if (text.contains("推荐") || text.contains("吃什么") || text.contains("招牌")
                || text.contains("有啥") || text.contains("特色") || text.contains("菜单")) {
            List<RecipeListVO> list = recipeService.recommendList(5);
            if (list.isEmpty()) {
                list = queryRecipes(null, 5);
            }
            if (list.isEmpty()) {
                return buildReply("暂时还没有菜品，稍后再来哦。", "local", actions, List.of());
            }
            actions.add(action("search", "查询了招牌推荐", null));
            String names = list.stream()
                    .map(item -> item.getName() + "(¥" + item.getPrice() + ")")
                    .reduce((a, b) -> a + "、" + b)
                    .orElse("");
            return buildReply("今天的招牌有：" + names + "。想点哪个？", "local", actions,
                    List.of("来一份" + list.get(0).getName(), "看看购物车"));
        }

        // 兜底：先给推荐
        List<RecipeListVO> list = recipeService.recommendList(3);
        if (!list.isEmpty()) {
            String names = list.stream().map(RecipeListVO::getName).reduce((a, b) -> a + "、" + b).orElse("");
            return buildReply("可以试试今天的招牌：" + names + "。直接说「来一份红烧肉」就能加购哦～",
                    "local", actions, List.of("今天有什么推荐", "看看购物车"));
        }
        return buildReply("想吃点什么？告诉我菜名就行，比如「来一份红烧肉」。", "local", actions,
                List.of("今天有什么推荐"));
    }

    // ===================== 公共辅助 =====================

    private List<RecipeListVO> queryRecipes(String keyword, int size) {
        RecipeQueryDTO query = new RecipeQueryDTO();
        query.setKeyword(keyword);
        query.setPageNum(1L);
        query.setPageSize((long) size);
        PageResult<RecipeListVO> page = recipeService.pageQuery(query);
        return page == null || page.getRecords() == null ? List.of() : page.getRecords();
    }

    /**
     * 在用户话术里匹配菜名：
     * 完整菜名 → 菜名尾部的 3 / 2 个字（「红烧肉」能命中「秘制红烧肉」）→ 去掉口语词后模糊搜索
     */
    private RecipeListVO matchRecipe(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        List<RecipeListVO> all = queryRecipes(null, 50);
        RecipeListVO best = null;
        int bestScore = 0;
        for (RecipeListVO item : all) {
            String name = item.getName();
            if (name == null) {
                continue;
            }
            int score = hitScore(text, name);
            if (score > bestScore) {
                bestScore = score;
                best = item;
            }
        }
        if (best != null) {
            return best;
        }
        String core = extractCore(text);
        if (StringUtils.hasText(core)) {
            List<RecipeListVO> searched = queryRecipes(core.length() > 8 ? core.substring(0, 8) : core, 3);
            if (!searched.isEmpty()) {
                return searched.get(0);
            }
        }
        return null;
    }

    /** 命中打分：完整菜名最高，其次菜名尾部三字、两字 */
    private int hitScore(String text, String name) {
        if (text.contains(name)) {
            return name.length() * 10;
        }
        if (name.length() >= 3 && text.contains(name.substring(name.length() - 3))) {
            return 3;
        }
        if (name.length() >= 2 && text.contains(name.substring(name.length() - 2))) {
            return 1;
        }
        return 0;
    }

    /** 去掉口语动词、数量词与标点，提取菜名核心词 */
    private String extractCore(String text) {
        return text
                .replaceAll("[来点要吃加上下再的吧啊呢帮我给一份个盘碗道两二三四五六七八九十0-9\\s，。！？、：]", "")
                .trim();
    }

    private int parseQuantity(String text) {
        Matcher matcher = QUANTITY_PATTERN.matcher(text);
        if (matcher.find()) {
            return Math.max(1, Integer.parseInt(matcher.group(1)));
        }
        if (text.contains("两") || text.contains("二")) {
            return 2;
        }
        if (text.contains("三")) {
            return 3;
        }
        return 1;
    }

    private String findPhone(String text) {
        Matcher matcher = PHONE_PATTERN.matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    @SuppressWarnings("unchecked")
    private List<ChatMessage> loadHistory(String cacheKey) {
        List<Map<String, Object>> raw = cacheSupport.get(cacheKey, List.class);
        List<ChatMessage> history = new ArrayList<>();
        if (raw != null) {
            for (Map<String, Object> item : raw) {
                Object role = item.get("role");
                Object content = item.get("content");
                if (role != null && content != null) {
                    history.add(new ChatMessage(String.valueOf(role), String.valueOf(content)));
                }
            }
        }
        return history;
    }

    private AssistantReplyVO buildReply(
            String reply, String mode, List<AssistantReplyVO.ActionVO> actions, List<String> suggestions) {
        AssistantReplyVO vo = new AssistantReplyVO();
        vo.setReply(reply);
        vo.setMode(mode);
        vo.setActions(actions);
        vo.setSuggestions(suggestions);
        return vo;
    }

    private AssistantReplyVO.ActionVO action(String type, String desc, String orderNo) {
        AssistantReplyVO.ActionVO vo = new AssistantReplyVO.ActionVO();
        vo.setType(type);
        vo.setDesc(desc);
        vo.setOrderNo(orderNo);
        return vo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class ChatMessage {
        private String role;
        private String content;
    }
}
