package com.example.systemcookies.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.systemcookies.entity.CookingStep;
import com.example.systemcookies.mapper.CookingStepMapper;
import com.example.systemcookies.service.CookingStepService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 烹饪步骤服务实现
 */
@Service
public class CookingStepServiceImpl extends ServiceImpl<CookingStepMapper, CookingStep> implements CookingStepService {

    @Override
    public List<CookingStep> listByRecipeId(Long recipeId) {
        return lambdaQuery()
                .eq(CookingStep::getRecipeId, recipeId)
                .orderByAsc(CookingStep::getStepNo)
                .list();
    }

    @Override
    public void replaceByRecipe(Long recipeId, List<CookingStep> steps) {
        getBaseMapper().deletePhysicallyByRecipeId(recipeId);
        if (CollectionUtils.isEmpty(steps)) {
            return;
        }
        steps.forEach(step -> {
            step.setId(null);
            step.setRecipeId(recipeId);
        });
        saveBatch(steps);
    }

    @Override
    public Map<Long, Integer> countByRecipeIds(List<Long> recipeIds) {
        Map<Long, Integer> result = new HashMap<>();
        if (CollectionUtils.isEmpty(recipeIds)) {
            return result;
        }
        List<Map<String, Object>> rows = listMaps(new QueryWrapper<CookingStep>()
                .select("recipe_id", "COUNT(*) AS cnt")
                .in("recipe_id", recipeIds)
                .groupBy("recipe_id"));
        for (Map<String, Object> row : rows) {
            Object recipeId = row.get("recipe_id");
            Object cnt = row.get("cnt");
            if (recipeId != null && cnt != null) {
                result.put(Long.valueOf(recipeId.toString()), Integer.parseInt(cnt.toString()));
            }
        }
        return result;
    }
}
