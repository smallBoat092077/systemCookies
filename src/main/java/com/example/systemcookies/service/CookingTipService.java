package com.example.systemcookies.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.example.systemcookies.entity.CookingTip;

import java.util.List;

/**
 * 烹饪技巧服务
 */
public interface CookingTipService extends IService<CookingTip> {

    /**
     * 查询某食谱的烹饪技巧
     */
    List<CookingTip> listByRecipeId(Long recipeId);

    /**
     * 全量替换某食谱的烹饪技巧
     */
    void replaceByRecipe(Long recipeId, List<CookingTip> tips);
}
