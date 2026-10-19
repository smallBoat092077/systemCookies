package com.example.systemcookies.controller.admin;

import com.example.systemcookies.common.PageResult;
import com.example.systemcookies.common.R;
import com.example.systemcookies.dto.RecipeQueryDTO;
import com.example.systemcookies.dto.RecipeSaveDTO;
import com.example.systemcookies.service.RecipeService;
import com.example.systemcookies.vo.RecipeDetailVO;
import com.example.systemcookies.vo.RecipeListVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理端食谱接口：维护菜谱与做法
 */
@RestController
@RequestMapping("/api/admin/recipes")
@RequiredArgsConstructor
public class RecipeAdminController {

    private final RecipeService recipeService;

    /**
     * 食谱分页（默认含下架，可用 onSaleOnly=true 只看上架）
     */
    @GetMapping
    public R<PageResult<RecipeListVO>> list(@ModelAttribute RecipeQueryDTO query) {
        return R.ok(recipeService.pageQuery(query));
    }

    /**
     * 食谱详情（含下架，供编辑回填）
     */
    @GetMapping("/{id}")
    public R<RecipeDetailVO> detail(@PathVariable Long id) {
        return R.ok(recipeService.getDetail(id));
    }

    /**
     * 新增食谱（含烹饪步骤 / 食材 / 调料 / 技巧）
     */
    @PostMapping
    public R<Map<String, Long>> save(@Valid @RequestBody RecipeSaveDTO dto) {
        Long id = recipeService.saveRecipe(dto);
        return R.ok("新增成功", Map.of("id", id));
    }

    /**
     * 修改食谱（子表全量替换）
     */
    @PutMapping
    public R<Void> update(@Valid @RequestBody RecipeSaveDTO dto) {
        recipeService.updateRecipe(dto);
        return R.ok();
    }

    /**
     * 删除食谱
     */
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        recipeService.removeRecipe(id);
        return R.ok();
    }

    /**
     * 上架 / 下架
     */
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        recipeService.updateStatus(id, status);
        return R.ok();
    }
}
