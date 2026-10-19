package com.example.systemcookies.common;

import java.util.Map;

/**
 * 订单状态常量与文案
 */
public final class OrderStatus {

    /** 待支付 */
    public static final int WAIT_PAY = 0;
    /** 已下单，待制作 */
    public static final int WAIT_COOK = 1;
    /** 制作中 */
    public static final int COOKING = 2;
    /** 已完成 */
    public static final int FINISHED = 3;
    /** 已取消 */
    public static final int CANCELED = 4;

    private static final Map<Integer, String> TEXT = Map.of(
            WAIT_PAY, "待支付",
            WAIT_COOK, "待制作",
            COOKING, "制作中",
            FINISHED, "已完成",
            CANCELED, "已取消");

    private OrderStatus() {
    }

    public static String text(Integer status) {
        return TEXT.getOrDefault(status, "未知状态");
    }

    public static boolean valid(Integer status) {
        return status != null && TEXT.containsKey(status);
    }

    /** 只有未完成的订单才允许取消 / 推进状态 */
    public static boolean isActive(Integer status) {
        return status != null && status != FINISHED && status != CANCELED;
    }
}
