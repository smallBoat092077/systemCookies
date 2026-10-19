package com.example.systemcookies.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.systemcookies.entity.RecipeSeasoning;
import com.example.systemcookies.mapper.RecipeSeasoningMapper;
import com.example.systemcookies.service.RecipeSeasoningService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 调料清单服务实现
 */
@Service
public class RecipeSeasoningServiceImpl extends ServiceImpl<RecipeSeasoningMapper, RecipeSeasoning>
        implements RecipeSeasoningService {

    @Override
    public List<RecipeSeasoning> listByRecipeId(Long recipeId) {
        return lambdaQuery()
                .eq(RecipeSeasoning::getRecipeId, recipeId)
                .orderByAsc(RecipeSeasoning::getSort)
                .orderByAsc(RecipeSeasoning::getId)
                .list();
    }

    @Override
    public void replaceByRecipe(Long recipeId, List<RecipeSeasoning> seasonings) {
        getBaseMapper().deletePhysicallyByRecipeId(recipeId);
        if (CollectionUtils.isEmpty(seasonings)) {
            return;
        }
        seasonings.forEach(item -> {
            item.setId(null);
            item.setRecipeId(recipeId);
        });
        saveBatch(seasonings);
    }
}
