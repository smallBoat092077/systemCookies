package com.example.systemcookies.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.systemcookies.common.BusinessException;
import com.example.systemcookies.common.CacheSupport;
import com.example.systemcookies.common.OrderStatus;
import com.example.systemcookies.common.PageResult;
import com.example.systemcookies.common.RedisKeys;
import com.example.systemcookies.dto.OrderCreateDTO;
import com.example.systemcookies.dto.OrderQueryDTO;
import com.example.systemcookies.entity.CartItem;
import com.example.systemcookies.entity.Order;
import com.example.systemcookies.entity.OrderItem;
import com.example.systemcookies.entity.Recipe;
import com.example.systemcookies.mapper.CartItemMapper;
import com.example.systemcookies.mapper.OrderItemMapper;
import com.example.systemcookies.mapper.OrderMapper;
import com.example.systemcookies.mapper.RecipeMapper;
import com.example.systemcookies.service.OrderService;
import com.example.systemcookies.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final RecipeMapper recipeMapper;
    private final CartItemMapper cartItemMapper;
    private final OrderItemMapper orderItemMapper;
    private final CacheSupport cacheSupport;

    // ==================== 下单 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(OrderCreateDTO dto) {
        Long userId = dto.getUserId() == null ? 1L : dto.getUserId();

        List<OrderItem> items = new ArrayList<>();
        List<Long> cartIds = new ArrayList<>();

        if (!CollectionUtils.isEmpty(dto.getCartItemIds())) {
            List<CartItem> carts = cartItemMapper.selectBatchIds(dto.getCartItemIds());
            if (carts.isEmpty()) {
                throw new BusinessException("购物车中没有勾选的菜品");
            }
            for (CartItem cart : carts) {
                if (!userId.equals(cart.getUserId())) {
                    throw new BusinessException("购物车条目不属于当前用户");
                }
                items.add(buildOrderItem(cart.getRecipeId(), cart.getQuantity()));
                cartIds.add(cart.getId());
            }
        } else {
            for (OrderCreateDTO.BuyItem buy : dto.getItems()) {
                items.add(buildOrderItem(buy.getRecipeId(), buy.getQuantity()));
            }
        }
        if (items.isEmpty()) {
            throw new BusinessException("请选择要下单的菜品");
        }

        BigDecimal total = BigDecimal.ZERO;
        int itemCount = 0;
        for (OrderItem item : items) {
            int affected = recipeMapper.deductStock(item.getRecipeId(), item.getQuantity());
            if (affected == 0) {
                throw new BusinessException("「" + item.getRecipeName() + "」库存不足，请调整份数");
            }
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            itemCount += item.getQuantity();
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setPayAmount(total);
        order.setItemCount(itemCount);
        order.setStatus(OrderStatus.WAIT_COOK);
        order.setContactName(dto.getContactName());
        order.setContactPhone(dto.getContactPhone());
        order.setAddress(dto.getAddress());
        order.setRemark(dto.getRemark());
        save(order);

        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        if (!cartIds.isEmpty()) {
            cartItemMapper.deletePhysicallyByIds(userId, cartIds);
        }

        evictRecipeCache(items.stream().map(OrderItem::getRecipeId).distinct().toList());
        log.info("下单成功：orderNo={}, amount={}, items={}", order.getOrderNo(), total, itemCount);
        return order.getId();
    }

    // ==================== 查询 ====================

    @Override
    public OrderVO getDetail(Long orderId, Long userId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (userId != null && !userId.equals(order.getUserId())) {
            throw new BusinessException(403, "无权查看该订单");
        }
        return toVO(order);
    }

    @Override
    public PageResult<OrderVO> pageQuery(OrderQueryDTO query) {
        if (query == null) {
            query = new OrderQueryDTO();
        }
        LambdaQueryWrapper<Order> wrapper = Wrappers.lambdaQuery();
        if (query.getUserId() != null) {
            wrapper.eq(Order::getUserId, query.getUserId());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Order::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(Order::getOrderNo, query.getKeyword().trim());
        }
        wrapper.orderByDesc(Order::getCreateTime);

        Page<Order> page = page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        List<Long> orderIds = page.getRecords().stream().map(Order::getId).toList();
        Map<Long, List<OrderItem>> itemMap = loadItems(orderIds);

        List<OrderVO> vos = page.getRecords().stream()
                .map(order -> {
                    OrderVO vo = new OrderVO();
                    BeanUtils.copyProperties(order, vo);
                    vo.setItems(itemMap.getOrDefault(order.getId(), List.of()));
                    vo.setStatusText(OrderStatus.text(order.getStatus()));
                    return vo;
                })
                .toList();

        PageResult<OrderVO> result = new PageResult<>();
        result.setRecords(vos);
        result.setTotal(page.getTotal());
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setPages(page.getPages());
        return result;
    }

    // ==================== 状态流转 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long userId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (userId != null && !userId.equals(order.getUserId())) {
            throw new BusinessException(403, "无权操作该订单");
        }
        if (!OrderStatus.isActive(order.getStatus())) {
            throw new BusinessException("订单已完成或已取消，不能重复操作");
        }

        List<OrderItem> items = loadItems(List.of(orderId)).getOrDefault(orderId, List.of());
        for (OrderItem item : items) {
            recipeMapper.restoreStock(item.getRecipeId(), item.getQuantity());
        }

        lambdaUpdate()
                .eq(Order::getId, orderId)
                .set(Order::getStatus, OrderStatus.CANCELED)
                .update();

        evictRecipeCache(items.stream().map(OrderItem::getRecipeId).distinct().toList());
    }

    @Override
    public void updateStatus(Long orderId, Integer status) {
        if (!OrderStatus.valid(status)) {
            throw new BusinessException("订单状态非法");
        }
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (status.equals(order.getStatus())) {
            return;
        }
        if (!OrderStatus.isActive(order.getStatus())) {
            throw new BusinessException("订单已完成或已取消，不能再变更状态");
        }
        // 管理端取消同样需要归还库存
        if (status == OrderStatus.CANCELED) {
            cancelOrder(orderId, null);
            return;
        }
        lambdaUpdate()
                .eq(Order::getId, orderId)
                .set(Order::getStatus, status)
                .update();
    }

    // ==================== 私有方法 ====================

    private OrderItem buildOrderItem(Long recipeId, Integer quantity) {
        Recipe recipe = recipeMapper.selectById(recipeId);
        if (recipe == null) {
            throw new BusinessException("菜品不存在");
        }
        if (recipe.getStatus() == null || recipe.getStatus() != 1) {
            throw new BusinessException("「" + recipe.getName() + "」已下架，无法下单");
        }
        int qty = quantity == null || quantity <= 0 ? 1 : quantity;
        OrderItem item = new OrderItem();
        item.setRecipeId(recipeId);
        item.setRecipeName(recipe.getName());
        item.setCoverUrl(recipe.getCoverUrl());
        item.setPrice(recipe.getPrice());
        item.setQuantity(qty);
        return item;
    }

    private OrderVO toVO(Order order) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setItems(loadItems(List.of(order.getId())).getOrDefault(order.getId(), List.of()));
        vo.setStatusText(OrderStatus.text(order.getStatus()));
        return vo;
    }

    private Map<Long, List<OrderItem>> loadItems(List<Long> orderIds) {
        if (CollectionUtils.isEmpty(orderIds)) {
            return Map.of();
        }
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .in(OrderItem::getOrderId, orderIds)
                        .orderByAsc(OrderItem::getId));
        return items.stream().collect(Collectors.groupingBy(OrderItem::getOrderId));
    }

    private String generateOrderNo() {
        String time = LocalDateTime.now().format(NO_FORMATTER);
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "SC" + time + random;
    }

    private void evictRecipeCache(List<Long> recipeIds) {
        if (CollectionUtils.isEmpty(recipeIds)) {
            return;
        }
        for (Long recipeId : recipeIds) {
            cacheSupport.delete(RedisKeys.recipeDetail(recipeId), RedisKeys.recipeSteps(recipeId));
        }
        cacheSupport.deleteByPrefix(RedisKeys.RECIPE_LIST);
    }
}
