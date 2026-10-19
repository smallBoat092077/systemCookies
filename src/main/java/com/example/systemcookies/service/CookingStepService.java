package com.example.systemcookies.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.example.systemcookies.entity.CookingStep;

import java.util.List;
import java.util.Map;

/**
 * 烹饪步骤服务
 */
public interface CookingStepService extends IService<CookingStep> {

    /**
     * 查询某食谱的全部烹饪步骤（按步骤序号升序）
     */
    List<CookingStep> listByRecipeId(Long recipeId);

    /**
     * 全量替换某食谱的烹饪步骤
     */
    void replaceByRecipe(Long recipeId, List<CookingStep> steps);

    /**
     * 批量统计食谱的步骤数量
     */
    Map<Long, Integer> countByRecipeIds(List<Long> recipeIds);
}
