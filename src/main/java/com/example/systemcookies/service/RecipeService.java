package com.example.systemcookies.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.example.systemcookies.common.PageResult;
import com.example.systemcookies.dto.RecipeQueryDTO;
import com.example.systemcookies.dto.RecipeSaveDTO;
import com.example.systemcookies.entity.CookingStep;
import com.example.systemcookies.entity.Recipe;
import com.example.systemcookies.vo.RecipeDetailVO;
import com.example.systemcookies.vo.RecipeListVO;

import java.util.List;

/**
 * 食谱服务：食谱主信息 + 烹饪步骤 + 食材 + 调料 + 烹饪技巧
 */
public interface RecipeService extends IService<Recipe> {

    /**
     * 新增食谱（含子表）
     *
     * @return 新增食谱ID
     */
    Long saveRecipe(RecipeSaveDTO dto);

    /**
     * 修改食谱（含子表，子表为全量替换）
     */
    void updateRecipe(RecipeSaveDTO dto);

    /**
     * 删除食谱（逻辑删除，同时清理子表）
     */
    void removeRecipe(Long id);

    /**
     * 上架 / 下架
     */
    void updateStatus(Long id, Integer status);

    /**
     * 食谱详情（含烹饪步骤、食材、调料、技巧）
     */
    RecipeDetailVO getDetail(Long id);

    /**
     * 只看烹饪步骤
     */
    List<CookingStep> getSteps(Long id);

    /**
     * 分页查询食谱
     */
    PageResult<RecipeListVO> pageQuery(RecipeQueryDTO query);

    /**
     * 推荐食谱
     */
    List<RecipeListVO> recommendList(int limit);
}
