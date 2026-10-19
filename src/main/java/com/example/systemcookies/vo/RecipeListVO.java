package com.example.systemcookies.vo;

import com.example.systemcookies.entity.Recipe;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 列表场景下的食谱卡片信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RecipeListVO extends Recipe {

    /** 分类名称 */
    private String categoryName;

    /** 烹饪步骤数量 */
    private Integer stepCount;
}
