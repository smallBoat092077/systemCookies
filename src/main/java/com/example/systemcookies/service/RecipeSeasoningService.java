package com.example.systemcookies.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.example.systemcookies.entity.RecipeSeasoning;

import java.util.List;

/**
 * 调料清单服务
 */
public interface RecipeSeasoningService extends IService<RecipeSeasoning> {

    /**
     * 查询某食谱的调料清单
     */
    List<RecipeSeasoning> listByRecipeId(Long recipeId);

    /**
     * 全量替换某食谱的调料清单
     */
    void replaceByRecipe(Long recipeId, List<RecipeSeasoning> seasonings);
}
