package com.example.systemcookies.common;

/**
 * 当前登录用户上下文：由 LoginInterceptor 填充，请求结束后清理
 */
public final class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Integer> ROLE = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(Long userId, Integer role) {
        USER_ID.set(userId);
        ROLE.set(role);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    /** 需要登录的接口里取当前用户ID，未登录时抛异常 */
    public static Long requiredUserId() {
        Long userId = USER_ID.get();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return userId;
    }

    public static Integer getRole() {
        return ROLE.get();
    }

    public static boolean isAdmin() {
        return Integer.valueOf(1).equals(ROLE.get());
    }

    public static void clear() {
        USER_ID.remove();
        ROLE.remove();
    }
}
