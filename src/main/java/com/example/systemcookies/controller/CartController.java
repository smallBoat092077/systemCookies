package com.example.systemcookies.controller;

import com.example.systemcookies.common.R;
import com.example.systemcookies.common.UserContext;
import com.example.systemcookies.dto.CartAddDTO;
import com.example.systemcookies.service.CartService;
import com.example.systemcookies.vo.CartVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 顾客端购物车接口：用户身份来自登录令牌，不再由前端传 userId
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * 购物车列表（带菜品实时价格与库存）
     */
    @GetMapping
    public R<List<CartVO>> list() {
        return R.ok(cartService.listCart(UserContext.requiredUserId()));
    }

    /**
     * 加入购物车，已存在则累加份数
     */
    @PostMapping
    public R<Void> add(@Valid @RequestBody CartAddDTO dto) {
        dto.setUserId(UserContext.requiredUserId());
        cartService.addItem(dto);
        return R.ok();
    }

    /**
     * 修改份数，传 0 表示移除
     */
    @PutMapping("/{recipeId}")
    public R<Void> updateQuantity(@PathVariable Long recipeId, @RequestParam Integer quantity) {
        CartAddDTO dto = new CartAddDTO();
        dto.setUserId(UserContext.requiredUserId());
        dto.setRecipeId(recipeId);
        dto.setQuantity(quantity);
        cartService.updateQuantity(dto);
        return R.ok();
    }

    /**
     * 移除某个菜品
     */
    @DeleteMapping("/{recipeId}")
    public R<Void> remove(@PathVariable Long recipeId) {
        cartService.removeItem(UserContext.requiredUserId(), recipeId);
        return R.ok();
    }

    /**
     * 清空购物车
     */
    @DeleteMapping
    public R<Void> clear() {
        cartService.clear(UserContext.requiredUserId());
        return R.ok();
    }

    /**
     * 购物车数量（用于角标）
     */
    @GetMapping("/count")
    public R<Map<String, Integer>> count() {
        return R.ok(Map.of("count", cartService.count(UserContext.requiredUserId())));
    }
}
