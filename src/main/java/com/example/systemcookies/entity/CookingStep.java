package com.example.systemcookies.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.systemcookies.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 烹饪步骤（Recipe 1:N）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cooking_step")
public class CookingStep extends BaseEntity {

    /** 食谱ID */
    private Long recipeId;

    /** 第几步，从 1 开始 */
    private Integer stepNo;

    /** 步骤标题 */
    private String title;

    /** 步骤正文 */
    private String content;

    /** 步骤配图 */
    private String imageUrl;

    /** 本步耗时（分钟） */
    private Integer duration;

    /** 本步注意要点 */
    private String tip;
}
