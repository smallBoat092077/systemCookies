package com.example.systemcookies.controller;

import com.example.systemcookies.common.PageResult;
import com.example.systemcookies.common.R;
import com.example.systemcookies.dto.RecipeQueryDTO;
import com.example.systemcookies.entity.CookingStep;
import com.example.systemcookies.entity.RecipeCategory;
import com.example.systemcookies.service.RecipeCategoryService;
import com.example.systemcookies.service.RecipeService;
import com.example.systemcookies.vo.RecipeDetailVO;
import com.example.systemcookies.vo.RecipeListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 顾客端食谱接口：看菜谱、看做法、看步骤
 */
@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;
    private final RecipeCategoryService recipeCategoryService;

    /**
     * 食谱列表（分页 + 筛选 + 排序）
     */
    @GetMapping
    public R<PageResult<RecipeListVO>> list(@ModelAttribute RecipeQueryDTO query) {
        query.setOnSaleOnly(true);
        return R.ok(recipeService.pageQuery(query));
    }

    /**
     * 推荐食谱
     */
    @GetMapping("/recommend")
    public R<List<RecipeListVO>> recommend(@RequestParam(defaultValue = "6") int limit) {
        return R.ok(recipeService.recommendList(limit));
    }

    /**
     * 食谱详情：基本信息 + 烹饪步骤 + 食材 + 调料 + 技巧
     */
    @GetMapping("/{id}")
    public R<RecipeDetailVO> detail(@PathVariable Long id) {
        RecipeDetailVO detail = recipeService.getDetail(id);
        if (detail.getStatus() != null && detail.getStatus() == 0) {
            return R.fail(404, "该菜品已下架");
        }
        return R.ok(detail);
    }

    /**
     * 只看烹饪步骤
     */
    @GetMapping("/{id}/steps")
    public R<List<CookingStep>> steps(@PathVariable Long id) {
        return R.ok(recipeService.getSteps(id));
    }

    /**
     * 食谱分类
     */
    @GetMapping("/categories")
    public R<List<RecipeCategory>> categories() {
        return R.ok(recipeCategoryService.listEnabled());
    }
}
