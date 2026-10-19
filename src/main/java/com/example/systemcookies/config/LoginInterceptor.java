package com.example.systemcookies.config;

import com.example.systemcookies.common.CacheSupport;
import com.example.systemcookies.common.R;
import com.example.systemcookies.common.RedisKeys;
import com.example.systemcookies.common.TokenInfo;
import com.example.systemcookies.common.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 登录拦截器：
 * - /api/auth/** 与 /api/recipes/** 公开（浏览菜谱无需登录）
 * - /api/admin/** 需登录且必须是管理员
 * - 其余 /api/** 需登录
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * 公开前缀：
     * /api/recipes 浏览菜谱、/api/check-in/recipe 查看某道菜的评论（发布与删除打卡仍要登录）
     */
    private static final List<String> PUBLIC_PREFIX =
            List.of("/api/recipes", "/api/check-in/recipe");

    private final CacheSupport cacheSupport;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String uri = request.getRequestURI();

        // 先还原身份：只要带了合法令牌就写入上下文。
        // 这样 /api/auth/info 这类接口也能拿到当前用户，公开接口也能区分「我发布的」。
        TokenInfo info = resolve(request);

        if (uri.startsWith("/api/admin")) {
            if (info == null) {
                write(response, 401, "请先登录");
                return false;
            }
            if (!Integer.valueOf(1).equals(info.getRole())) {
                write(response, 403, "无权访问管理端接口");
                return false;
            }
            applyContext(info);
            return true;
        }

        if (uri.startsWith("/api/auth")) {
            applyContext(info);
            return true;
        }
        // 公开：某条打卡的回复列表 GET /api/check-in/{id}/replies
        if ("GET".equalsIgnoreCase(request.getMethod())
                && uri.startsWith("/api/check-in/")
                && uri.endsWith("/replies")) {
            applyContext(info);
            return true;
        }
        for (String prefix : PUBLIC_PREFIX) {
            if (uri.startsWith(prefix)) {
                applyContext(info);
                return true;
            }
        }

        if (info == null) {
            write(response, 401, "请先登录");
            return false;
        }
        applyContext(info);
        return true;
    }

    private void applyContext(TokenInfo info) {
        if (info != null) {
            UserContext.set(info.getUserId(), info.getRole());
        }
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    /** 从 Authorization 头或 token 参数里取令牌，还原用户身份 */
    private TokenInfo resolve(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (!StringUtils.hasText(token)) {
            token = request.getParameter("token");
        }
        if (!StringUtils.hasText(token)) {
            return null;
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return cacheSupport.get(RedisKeys.authToken(token), TokenInfo.class);
    }

    private void write(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(R.fail(status, message)));
    }
}
