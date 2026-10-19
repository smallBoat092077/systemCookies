package com.example.systemcookies.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.systemcookies.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 购物车条目：一个用户对一道菜只有一条记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cart_item")
public class CartItem extends BaseEntity {

    /** 用户ID，演示阶段默认 1 */
    private Long userId;

    /** 食谱ID */
    private Long recipeId;

    /** 份数 */
    private Integer quantity;
}
