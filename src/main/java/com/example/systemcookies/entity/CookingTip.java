package com.example.systemcookies.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.systemcookies.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 烹饪技巧（Recipe 1:N）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cooking_tip")
public class CookingTip extends BaseEntity {

    /** 食谱ID */
    private Long recipeId;

    /** 技巧标题 */
    private String title;

    /** 技巧说明 */
    private String content;

    /** 排序值 */
    private Integer sort;
}
