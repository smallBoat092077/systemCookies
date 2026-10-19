package com.example.systemcookies.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.systemcookies.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 食谱（菜谱）主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("recipe")
public class Recipe extends BaseEntity {

    private Long categoryId;
    private String name;
    private String subtitle;
    private String coverUrl;
    private String description;
    private String cuisine;
    private String taste;
    private String cookMethod;
    private Integer difficulty;
    private Integer cookTime;
    private Integer servings;
    private Integer calories;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer sales;
    private Integer isRecommend;
    private Integer status;
}
