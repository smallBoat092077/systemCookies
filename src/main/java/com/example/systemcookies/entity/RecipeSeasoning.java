package com.example.systemcookies.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.systemcookies.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 调料清单（Recipe 1:N）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("recipe_seasoning")
public class RecipeSeasoning extends BaseEntity {

    /** 食谱ID */
    private Long recipeId;

    /** 调料名称 */
    private String name;

    /** 用量描述 */
    private String amount;

    /** 计量单位 */
    private String unit;

    /** 备注 */
    private String note;

    /** 排序值 */
    private Integer sort;
}
