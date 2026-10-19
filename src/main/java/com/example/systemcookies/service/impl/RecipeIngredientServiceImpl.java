package com.example.systemcookies.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.systemcookies.entity.RecipeIngredient;
import com.example.systemcookies.mapper.RecipeIngredientMapper;
import com.example.systemcookies.service.RecipeIngredientService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 食材清单服务实现
 */
@Service
public class RecipeIngredientServiceImpl extends ServiceImpl<RecipeIngredientMapper, RecipeIngredient>
        implements RecipeIngredientService {

    @Override
    public List<RecipeIngredient> listByRecipeId(Long recipeId) {
        return lambdaQuery()
                .eq(RecipeIngredient::getRecipeId, recipeId)
                .orderByDesc(RecipeIngredient::getIsMain)
                .orderByAsc(RecipeIngredient::getSort)
                .orderByAsc(RecipeIngredient::getId)
                .list();
    }

    @Override
    public void replaceByRecipe(Long recipeId, List<RecipeIngredient> ingredients) {
        getBaseMapper().deletePhysicallyByRecipeId(recipeId);
        if (CollectionUtils.isEmpty(ingredients)) {
            return;
        }
        ingredients.forEach(item -> {
            item.setId(null);
            item.setRecipeId(recipeId);
        });
        saveBatch(ingredients);
    }
}
