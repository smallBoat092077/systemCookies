package com.example.systemcookies.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.systemcookies.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单项：下单时的菜品快照，避免后续改价改菜名影响历史订单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_item")
public class OrderItem extends BaseEntity {

    /** 订单ID */
    private Long orderId;

    /** 食谱ID */
    private Long recipeId;

    /** 菜名快照 */
    private String recipeName;

    /** 封面快照 */
    private String coverUrl;

    /** 单价快照 */
    private BigDecimal price;

    /** 份数 */
    private Integer quantity;
}
