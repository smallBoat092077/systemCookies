package com.example.systemcookies.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.example.systemcookies.entity.RecipeCategory;

import java.util.List;

/**
 * 食谱分类服务
 */
public interface RecipeCategoryService extends IService<RecipeCategory> {

    /**
     * 查询全部启用分类（带缓存）
     */
    List<RecipeCategory> listEnabled();

    /**
     * 新增分类
     */
    Long saveCategory(RecipeCategory category);

    /**
     * 修改分类
     */
    void updateCategory(RecipeCategory category);

    /**
     * 删除分类（该分类下无食谱时才允许删除）
     */
    void removeCategory(Long id);
}
