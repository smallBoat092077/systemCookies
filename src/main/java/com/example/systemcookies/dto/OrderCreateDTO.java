package com.example.systemcookies.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 下单入参：支持「购物车勾选结算」与「立即购买」两种方式
 */
@Data
public class OrderCreateDTO {

    /** 用户ID，演示阶段默认 1 */
    private Long userId = 1L;

    /** 购物车条目ID（勾选结算时传） */
    private List<Long> cartItemIds;

    /** 立即购买的菜品（不走购物车时传） */
    private List<BuyItem> items;

    private String contactName;

    private String contactPhone;

    private String address;

    /** 备注：口味、忌口等 */
    private String remark;

    @Data
    public static class BuyItem {
        @NotNull(message = "请选择菜品")
        private Long recipeId;

        @Min(value = 1, message = "份数至少为 1")
        private Integer quantity = 1;
    }

    public List<BuyItem> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }
}
