package com.example.systemcookies.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车条目：菜品信息 + 份数 + 小计
 */
@Data
public class CartVO {

    /** 购物车条目ID */
    private Long id;

    /** 食谱ID */
    private Long recipeId;

    /** 菜名 */
    private String name;

    /** 副标题 */
    private String subtitle;

    /** 封面 */
    private String coverUrl;

    /** 单价 */
    private BigDecimal price;

    /** 份数 */
    private Integer quantity;

    /** 剩余库存 */
    private Integer stock;

    /** 上架状态：1在售 0下架 */
    private Integer status;

    /** 小计 = 单价 × 份数 */
    private BigDecimal subtotal;

    /** 该条目是否可选（下架或库存不足时不可结算） */
    private Boolean available;
}
