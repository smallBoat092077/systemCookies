package com.example.systemcookies.controller.admin;

import com.example.systemcookies.common.R;
import com.example.systemcookies.entity.RecipeCategory;
import com.example.systemcookies.service.RecipeCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 管理端食谱分类接口
 */
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class RecipeCategoryAdminController {

    private final RecipeCategoryService recipeCategoryService;

    @GetMapping
    public R<List<RecipeCategory>> list() {
        return R.ok(recipeCategoryService.list());
    }

    @PostMapping
    public R<Map<String, Long>> save(@RequestBody RecipeCategory category) {
        Long id = recipeCategoryService.saveCategory(category);
        return R.ok("新增成功", Map.of("id", id));
    }

    @PutMapping
    public R<Void> update(@RequestBody RecipeCategory category) {
        recipeCategoryService.updateCategory(category);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        recipeCategoryService.removeCategory(id);
        return R.ok();
    }
}
