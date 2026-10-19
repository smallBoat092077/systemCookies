package com.example.systemcookies.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.systemcookies.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 食材清单（Recipe 1:N）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("recipe_ingredient")
public class RecipeIngredient extends BaseEntity {

    /** 食谱ID */
    private Long recipeId;

    /** 食材名称 */
    private String name;

    /** 用量描述 */
    private String amount;

    /** 计量单位 */
    private String unit;

    /** 是否主料：1主料 0辅料 */
    private Integer isMain;

    /** 备注 */
    private String note;

    /** 排序值 */
    private Integer sort;
}
