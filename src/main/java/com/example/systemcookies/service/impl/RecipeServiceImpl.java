package com.example.systemcookies.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.systemcookies.common.BusinessException;
import com.example.systemcookies.common.CacheSupport;
import com.example.systemcookies.common.PageResult;
import com.example.systemcookies.common.RedisKeys;
import com.example.systemcookies.dto.RecipeQueryDTO;
import com.example.systemcookies.dto.RecipeSaveDTO;
import com.example.systemcookies.entity.CookingStep;
import com.example.systemcookies.entity.CookingTip;
import com.example.systemcookies.entity.Recipe;
import com.example.systemcookies.entity.RecipeCategory;
import com.example.systemcookies.entity.RecipeIngredient;
import com.example.systemcookies.entity.RecipeSeasoning;
import com.example.systemcookies.mapper.RecipeMapper;
import com.example.systemcookies.service.CookingStepService;
import com.example.systemcookies.service.CookingTipService;
import com.example.systemcookies.service.RecipeCategoryService;
import com.example.systemcookies.service.RecipeIngredientService;
import com.example.systemcookies.service.RecipeSeasoningService;
import com.example.systemcookies.service.RecipeService;
import com.example.systemcookies.vo.RecipeDetailVO;
import com.example.systemcookies.vo.RecipeListVO;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 食谱服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeServiceImpl extends ServiceImpl<RecipeMapper, Recipe> implements RecipeService {

    private final RecipeCategoryService recipeCategoryService;
    private final CookingStepService cookingStepService;
    private final RecipeIngredientService recipeIngredientService;
    private final RecipeSeasoningService recipeSeasoningService;
    private final CookingTipService cookingTipService;
    private final CacheSupport cacheSupport;

    @Value("${recipe.cache.enabled:true}")
    private boolean cacheEnabled;

    @Value("${recipe.cache.ttl-minutes:30}")
    private long ttlMinutes;

    // ==================== 新增 / 修改 ====================

    @Override
    public Long saveRecipe(RecipeSaveDTO dto) {
        Recipe recipe = toEntity(dto);
        recipe.setId(null);
        recipe.setSales(0);
        save(recipe);
        saveChildren(recipe.getId(), dto);
        evictListCache();
        return recipe.getId();
    }

    @Override
    public void updateRecipe(RecipeSaveDTO dto) {
        Long id = dto.getId();
        if (id == null) {
            throw new BusinessException("食谱ID不能为空");
        }
        if (getById(id) == null) {
            throw new BusinessException("食谱不存在");
        }
        Recipe recipe = toEntity(dto);
        recipe.setId(id);
        recipe.setSales(null);
        updateById(recipe);
        saveChildren(id, dto);
        evictRecipeCache(id);
        evictListCache();
    }

    @Override
    public void removeRecipe(Long id) {
        Recipe recipe = getById(id);
        if (recipe == null) {
            throw new BusinessException("食谱不存在");
        }
        removeById(id);
        cookingStepService.replaceByRecipe(id, Collections.emptyList());
        recipeIngredientService.replaceByRecipe(id, Collections.emptyList());
        recipeSeasoningService.replaceByRecipe(id, Collections.emptyList());
        cookingTipService.replaceByRecipe(id, Collections.emptyList());
        evictRecipeCache(id);
        evictListCache();
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Recipe recipe = getById(id);
        if (recipe == null) {
            throw new BusinessException("食谱不存在");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态取值非法");
        }
        lambdaUpdate().eq(Recipe::getId, id).set(Recipe::getStatus, status).update();
        evictRecipeCache(id);
        evictListCache();
    }

    // ==================== 查询 ====================

    @Override
    public RecipeDetailVO getDetail(Long id) {
        String cacheKey = RedisKeys.recipeDetail(id);
        if (cacheEnabled) {
            RecipeDetailVO cached = cacheSupport.get(cacheKey, RecipeDetailVO.class);
            if (cached != null) {
                return cached;
            }
        }
        Recipe recipe = getById(id);
        if (recipe == null) {
            throw new BusinessException(404, "食谱不存在");
        }
        RecipeDetailVO vo = new RecipeDetailVO();
        BeanUtils.copyProperties(recipe, vo);
        vo.setCategoryName(resolveCategoryName(recipe.getCategoryId()));
        vo.setSteps(cookingStepService.listByRecipeId(id));
        vo.setIngredients(recipeIngredientService.listByRecipeId(id));
        vo.setSeasonings(recipeSeasoningService.listByRecipeId(id));
        vo.setTips(cookingTipService.listByRecipeId(id));
        if (cacheEnabled) {
            cacheSupport.set(cacheKey, vo, Duration.ofMinutes(ttlMinutes));
        }
        return vo;
    }

    @Override
    public List<CookingStep> getSteps(Long id) {
        String cacheKey = RedisKeys.recipeSteps(id);
        if (cacheEnabled) {
            List<CookingStep> cached = cacheSupport.get(cacheKey, new TypeReference<List<CookingStep>>() {
            });
            if (cached != null) {
                return cached;
            }
        }
        List<CookingStep> steps = cookingStepService.listByRecipeId(id);
        if (cacheEnabled) {
            cacheSupport.set(cacheKey, steps, Duration.ofMinutes(ttlMinutes));
        }
        return steps;
    }

    @Override
    public PageResult<RecipeListVO> pageQuery(RecipeQueryDTO query) {
        if (query == null) {
            query = new RecipeQueryDTO();
        }
        String cacheKey = RedisKeys.RECIPE_LIST + fingerprint(query);
        if (cacheEnabled) {
            PageResult<RecipeListVO> cached = cacheSupport.get(cacheKey,
                    new TypeReference<PageResult<RecipeListVO>>() {
                    });
            if (cached != null) {
                return cached;
            }
        }
        LambdaQueryWrapper<Recipe> wrapper = buildQueryWrapper(query);
        Page<Recipe> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<Recipe> result = page(page, wrapper);

        PageResult<RecipeListVO> pageResult = new PageResult<>();
        pageResult.setRecords(convert(result.getRecords()));
        pageResult.setTotal(result.getTotal());
        pageResult.setPageNum(result.getCurrent());
        pageResult.setPageSize(result.getSize());
        pageResult.setPages(result.getPages());
        if (cacheEnabled) {
            cacheSupport.set(cacheKey, pageResult, Duration.ofMinutes(Math.min(ttlMinutes, 10)));
        }
        return pageResult;
    }

    @Override
    public List<RecipeListVO> recommendList(int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        Page<Recipe> page = new Page<>(1, size);
        Page<Recipe> result = page(page, Wrappers.<Recipe>lambdaQuery()
                .eq(Recipe::getStatus, 1)
                .eq(Recipe::getIsRecommend, 1)
                .orderByDesc(Recipe::getSales)
                .orderByDesc(Recipe::getId));
        return convert(result.getRecords());
    }

    // ==================== 私有方法 ====================

    private Recipe toEntity(RecipeSaveDTO dto) {
        Recipe recipe = new Recipe();
        BeanUtils.copyProperties(dto, recipe);
        return recipe;
    }

    private void saveChildren(Long recipeId, RecipeSaveDTO dto) {
        List<CookingStep> steps = new ArrayList<>();
        List<RecipeSaveDTO.StepItem> stepItems = dto.getSteps() == null ? List.of() : dto.getSteps();
        for (int i = 0; i < stepItems.size(); i++) {
            RecipeSaveDTO.StepItem item = stepItems.get(i);
            CookingStep step = new CookingStep();
            BeanUtils.copyProperties(item, step);
            if (step.getStepNo() == null) {
                step.setStepNo(i + 1);
            }
            if (step.getDuration() == null) {
                step.setDuration(0);
            }
            steps.add(step);
        }
        cookingStepService.replaceByRecipe(recipeId, steps);

        List<RecipeIngredient> ingredients = new ArrayList<>();
        List<RecipeSaveDTO.IngredientItem> ingredientItems =
                dto.getIngredients() == null ? List.of() : dto.getIngredients();
        for (int i = 0; i < ingredientItems.size(); i++) {
            RecipeSaveDTO.IngredientItem item = ingredientItems.get(i);
            RecipeIngredient entity = new RecipeIngredient();
            BeanUtils.copyProperties(item, entity);
            if (entity.getIsMain() == null) {
                entity.setIsMain(0);
            }
            if (entity.getSort() == null) {
                entity.setSort(i);
            }
            ingredients.add(entity);
        }
        recipeIngredientService.replaceByRecipe(recipeId, ingredients);

        List<RecipeSeasoning> seasonings = new ArrayList<>();
        List<RecipeSaveDTO.SeasoningItem> seasoningItems =
                dto.getSeasonings() == null ? List.of() : dto.getSeasonings();
        for (int i = 0; i < seasoningItems.size(); i++) {
            RecipeSaveDTO.SeasoningItem item = seasoningItems.get(i);
            RecipeSeasoning entity = new RecipeSeasoning();
            BeanUtils.copyProperties(item, entity);
            if (entity.getSort() == null) {
                entity.setSort(i);
            }
            seasonings.add(entity);
        }
        recipeSeasoningService.replaceByRecipe(recipeId, seasonings);

        List<CookingTip> tips = new ArrayList<>();
        List<RecipeSaveDTO.TipItem> tipItems = dto.getTips() == null ? List.of() : dto.getTips();
        for (int i = 0; i < tipItems.size(); i++) {
            RecipeSaveDTO.TipItem item = tipItems.get(i);
            CookingTip entity = new CookingTip();
            BeanUtils.copyProperties(item, entity);
            if (entity.getSort() == null) {
                entity.setSort(i);
            }
            tips.add(entity);
        }
        cookingTipService.replaceByRecipe(recipeId, tips);
    }

    private LambdaQueryWrapper<Recipe> buildQueryWrapper(RecipeQueryDTO query) {
        LambdaQueryWrapper<Recipe> wrapper = Wrappers.lambdaQuery();
        if (Boolean.TRUE.equals(query.getOnSaleOnly())) {
            wrapper.eq(Recipe::getStatus, 1);
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(w -> w.like(Recipe::getName, keyword)
                    .or().like(Recipe::getSubtitle, keyword)
                    .or().like(Recipe::getDescription, keyword)
                    .or().like(Recipe::getCuisine, keyword));
        }
        if (query.getCategoryId() != null) {
            wrapper.eq(Recipe::getCategoryId, query.getCategoryId());
        }
        if (StringUtils.hasText(query.getTaste())) {
            wrapper.eq(Recipe::getTaste, query.getTaste());
        }
        if (StringUtils.hasText(query.getCookMethod())) {
            wrapper.eq(Recipe::getCookMethod, query.getCookMethod());
        }
        if (query.getDifficulty() != null) {
            wrapper.eq(Recipe::getDifficulty, query.getDifficulty());
        }
        if (query.getMinPrice() != null) {
            wrapper.ge(Recipe::getPrice, query.getMinPrice());
        }
        if (query.getMaxPrice() != null) {
            wrapper.le(Recipe::getPrice, query.getMaxPrice());
        }
        if (Boolean.TRUE.equals(query.getRecommendOnly())) {
            wrapper.eq(Recipe::getIsRecommend, 1);
        }
        applyOrder(wrapper, query.getOrderBy());
        return wrapper;
    }

    private void applyOrder(LambdaQueryWrapper<Recipe> wrapper, String orderBy) {
        if (!StringUtils.hasText(orderBy)) {
            orderBy = "default";
        }
        switch (orderBy) {
            case "sales" -> wrapper.orderByDesc(Recipe::getSales);
            case "priceAsc" -> wrapper.orderByAsc(Recipe::getPrice);
            case "priceDesc" -> wrapper.orderByDesc(Recipe::getPrice);
            case "time" -> wrapper.orderByAsc(Recipe::getCookTime);
            case "newest" -> wrapper.orderByDesc(Recipe::getCreateTime);
            default -> wrapper.orderByDesc(Recipe::getIsRecommend)
                    .orderByDesc(Recipe::getSales)
                    .orderByDesc(Recipe::getId);
        }
    }

    private List<RecipeListVO> convert(List<Recipe> recipes) {
        if (recipes == null || recipes.isEmpty()) {
            return List.of();
        }
        List<Long> categoryIds = recipes.stream()
                .map(Recipe::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> categoryNameMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            List<RecipeCategory> categories = recipeCategoryService.listByIds(categoryIds);
            if (categories != null) {
                categories.forEach(c -> categoryNameMap.put(c.getId(), c.getName()));
            }
        }
        List<Long> recipeIds = recipes.stream().map(Recipe::getId).toList();
        Map<Long, Integer> stepCountMap = cookingStepService.countByRecipeIds(recipeIds);

        List<RecipeListVO> vos = new ArrayList<>(recipes.size());
        for (Recipe recipe : recipes) {
            RecipeListVO vo = new RecipeListVO();
            BeanUtils.copyProperties(recipe, vo);
            vo.setCategoryName(categoryNameMap.get(recipe.getCategoryId()));
            vo.setStepCount(stepCountMap.getOrDefault(recipe.getId(), 0));
            vos.add(vo);
        }
        return vos;
    }

    private String resolveCategoryName(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            return null;
        }
        RecipeCategory category = recipeCategoryService.getById(categoryId);
        return category == null ? null : category.getName();
    }

    private String fingerprint(RecipeQueryDTO query) {
        String raw = String.join("|",
                String.valueOf(query.getPageNum()),
                String.valueOf(query.getPageSize()),
                Objects.toString(query.getKeyword(), ""),
                Objects.toString(query.getCategoryId(), ""),
                Objects.toString(query.getTaste(), ""),
                Objects.toString(query.getCookMethod(), ""),
                Objects.toString(query.getDifficulty(), ""),
                Objects.toString(query.getMinPrice(), ""),
                Objects.toString(query.getMaxPrice(), ""),
                Objects.toString(query.getRecommendOnly(), ""),
                Objects.toString(query.getOnSaleOnly(), ""),
                Objects.toString(query.getOrderBy(), ""));
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }

    private void evictRecipeCache(Long recipeId) {
        cacheSupport.delete(RedisKeys.recipeDetail(recipeId), RedisKeys.recipeSteps(recipeId));
    }

    private void evictListCache() {
        cacheSupport.deleteByPrefix(RedisKeys.RECIPE_LIST);
    }
}
