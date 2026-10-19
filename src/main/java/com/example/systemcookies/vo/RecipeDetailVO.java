package com.example.systemcookies.vo;

import com.example.systemcookies.entity.CookingStep;
import com.example.systemcookies.entity.CookingTip;
import com.example.systemcookies.entity.Recipe;
import com.example.systemcookies.entity.RecipeIngredient;
import com.example.systemcookies.entity.RecipeSeasoning;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 食谱详情：主信息 + 烹饪步骤 + 食材 + 调料 + 烹饪技巧
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RecipeDetailVO extends Recipe {

    /** 分类名称 */
    private String categoryName;

    /** 烹饪步骤（按 step_no 升序） */
    private List<CookingStep> steps = List.of();

    /** 食材清单 */
    private List<RecipeIngredient> ingredients = List.of();

    /** 调料清单 */
    private List<RecipeSeasoning> seasonings = List.of();

    /** 烹饪技巧 */
    private List<CookingTip> tips = List.of();
}
