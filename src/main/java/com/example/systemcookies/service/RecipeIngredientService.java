package com.example.systemcookies.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.example.systemcookies.entity.RecipeIngredient;

import java.util.List;

/**
 * 食材清单服务
 */
public interface RecipeIngredientService extends IService<RecipeIngredient> {

    /**
     * 查询某食谱的食材清单（主料优先，再按排序值）
     */
    List<RecipeIngredient> listByRecipeId(Long recipeId);

    /**
     * 全量替换某食谱的食材清单
     */
    void replaceByRecipe(Long recipeId, List<RecipeIngredient> ingredients);
}
