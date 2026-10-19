package com.example.systemcookies.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.systemcookies.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 食谱分类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("recipe_category")
public class RecipeCategory extends BaseEntity {

    /** 分类名称 */
    private String name;

    /** 父分类ID，0 表示顶级分类 */
    private Long parentId;

    /** 分类图标 */
    private String icon;

    /** 排序值，越小越靠前 */
    private Integer sort;

    /** 状态：1启用 0禁用 */
    private Integer status;
}
