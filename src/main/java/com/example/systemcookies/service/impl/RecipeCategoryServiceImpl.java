package com.example.systemcookies.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.systemcookies.common.BusinessException;
import com.example.systemcookies.common.CacheSupport;
import com.example.systemcookies.common.RedisKeys;
import com.example.systemcookies.entity.Recipe;
import com.example.systemcookies.entity.RecipeCategory;
import com.example.systemcookies.mapper.RecipeCategoryMapper;
import com.example.systemcookies.mapper.RecipeMapper;
import com.example.systemcookies.service.RecipeCategoryService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;

/**
 * 食谱分类服务实现
 */
@Service
@RequiredArgsConstructor
public class RecipeCategoryServiceImpl extends ServiceImpl<RecipeCategoryMapper, RecipeCategory>
        implements RecipeCategoryService {

    private final RecipeMapper recipeMapper;
    private final CacheSupport cacheSupport;

    @Override
    public List<RecipeCategory> listEnabled() {
        List<RecipeCategory> cached = cacheSupport.get(RedisKeys.CATEGORY_LIST,
                new TypeReference<List<RecipeCategory>>() {
                });
        if (cached != null) {
            return cached;
        }
        List<RecipeCategory> list = lambdaQuery()
                .eq(RecipeCategory::getStatus, 1)
                .orderByAsc(RecipeCategory::getSort)
                .orderByAsc(RecipeCategory::getId)
                .list();
        cacheSupport.set(RedisKeys.CATEGORY_LIST, list, Duration.ofMinutes(30));
        return list;
    }

    @Override
    public Long saveCategory(RecipeCategory category) {
        if (!StringUtils.hasText(category.getName())) {
            throw new BusinessException("分类名称不能为空");
        }
        long count = lambdaQuery().eq(RecipeCategory::getName, category.getName()).count();
        if (count > 0) {
            throw new BusinessException("分类名称已存在");
        }
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        save(category);
        cacheSupport.delete(RedisKeys.CATEGORY_LIST);
        return category.getId();
    }

    @Override
    public void updateCategory(RecipeCategory category) {
        if (category.getId() == null) {
            throw new BusinessException("分类ID不能为空");
        }
        if (getById(category.getId()) == null) {
            throw new BusinessException("分类不存在");
        }
        updateById(category);
        cacheSupport.delete(RedisKeys.CATEGORY_LIST);
    }

    @Override
    public void removeCategory(Long id) {
        RecipeCategory category = getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        Long used = recipeMapper.selectCount(new LambdaQueryWrapper<Recipe>().eq(Recipe::getCategoryId, id));
        if (used != null && used > 0) {
            throw new BusinessException("该分类下仍有 " + used + " 个食谱，不能删除");
        }
        removeById(id);
        cacheSupport.delete(RedisKeys.CATEGORY_LIST);
    }
}
