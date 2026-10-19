package com.example.systemcookies.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.systemcookies.common.BusinessException;
import com.example.systemcookies.dto.CartAddDTO;
import com.example.systemcookies.entity.CartItem;
import com.example.systemcookies.entity.Recipe;
import com.example.systemcookies.mapper.CartItemMapper;
import com.example.systemcookies.mapper.RecipeMapper;
import com.example.systemcookies.service.CartService;
import com.example.systemcookies.vo.CartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 购物车服务实现
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartItemMapper, CartItem> implements CartService {

    private final RecipeMapper recipeMapper;

    @Override
    public List<CartVO> listCart(Long userId) {
        List<CartItem> items = lambdaQuery()
                .eq(CartItem::getUserId, userId)
                .orderByDesc(CartItem::getCreateTime)
                .list();
        if (CollectionUtils.isEmpty(items)) {
            return List.of();
        }
        List<Long> recipeIds = items.stream().map(CartItem::getRecipeId).distinct().toList();
        Map<Long, Recipe> recipeMap = recipeMapper.selectBatchIds(recipeIds)
                .stream()
                .collect(Collectors.toMap(Recipe::getId, Function.identity()));

        List<CartVO> vos = new ArrayList<>(items.size());
        for (CartItem item : items) {
            Recipe recipe = recipeMap.get(item.getRecipeId());
            if (recipe == null) {
                // 菜品已被删除，跳过该条目
                continue;
            }
            CartVO vo = new CartVO();
            vo.setId(item.getId());
            vo.setRecipeId(item.getRecipeId());
            vo.setName(recipe.getName());
            vo.setSubtitle(recipe.getSubtitle());
            vo.setCoverUrl(recipe.getCoverUrl());
            vo.setPrice(recipe.getPrice());
            vo.setQuantity(item.getQuantity());
            vo.setStock(recipe.getStock());
            vo.setStatus(recipe.getStatus());
            vo.setSubtotal(recipe.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            vo.setAvailable(
                    recipe.getStatus() != null
                            && recipe.getStatus() == 1
                            && recipe.getStock() != null
                            && recipe.getStock() >= item.getQuantity());
            vos.add(vo);
        }
        return vos;
    }

    @Override
    public void addItem(CartAddDTO dto) {
        Recipe recipe = recipeMapper.selectById(dto.getRecipeId());
        if (recipe == null) {
            throw new BusinessException("菜品不存在");
        }
        if (recipe.getStatus() == null || recipe.getStatus() != 1) {
            throw new BusinessException("「" + recipe.getName() + "」已下架，无法加入购物车");
        }
        CartItem exist = lambdaQuery()
                .eq(CartItem::getUserId, dto.getUserId())
                .eq(CartItem::getRecipeId, dto.getRecipeId())
                .one();
        if (exist == null) {
            CartItem item = new CartItem();
            item.setUserId(dto.getUserId());
            item.setRecipeId(dto.getRecipeId());
            item.setQuantity(dto.getQuantity());
            save(item);
            return;
        }
        int quantity = exist.getQuantity() + dto.getQuantity();
        lambdaUpdate()
                .eq(CartItem::getId, exist.getId())
                .set(CartItem::getQuantity, quantity)
                .update();
    }

    @Override
    public void updateQuantity(CartAddDTO dto) {
        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            removeItem(dto.getUserId(), dto.getRecipeId());
            return;
        }
        CartItem exist = lambdaQuery()
                .eq(CartItem::getUserId, dto.getUserId())
                .eq(CartItem::getRecipeId, dto.getRecipeId())
                .one();
        if (exist == null) {
            throw new BusinessException("购物车中没有这道菜");
        }
        Recipe recipe = recipeMapper.selectById(dto.getRecipeId());
        if (recipe != null && recipe.getStock() != null && dto.getQuantity() > recipe.getStock()) {
            throw new BusinessException("「" + recipe.getName() + "」库存仅剩 " + recipe.getStock() + " 份");
        }
        lambdaUpdate()
                .eq(CartItem::getId, exist.getId())
                .set(CartItem::getQuantity, dto.getQuantity())
                .update();
    }

    @Override
    public void removeItem(Long userId, Long recipeId) {
        getBaseMapper().deletePhysically(userId, recipeId);
    }

    @Override
    public void clear(Long userId) {
        getBaseMapper().clearByUser(userId);
    }

    @Override
    public int count(Long userId) {
        return Math.toIntExact(lambdaQuery().eq(CartItem::getUserId, userId).count());
    }
}
