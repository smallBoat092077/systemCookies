package com.example.systemcookies.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.example.systemcookies.dto.CartAddDTO;
import com.example.systemcookies.entity.CartItem;
import com.example.systemcookies.vo.CartVO;

import java.util.List;

/**
 * 购物车服务
 */
public interface CartService extends IService<CartItem> {

    /**
     * 购物车列表（带菜品实时信息）
     */
    List<CartVO> listCart(Long userId);

    /**
     * 加入购物车：已存在则累加份数
     */
    void addItem(CartAddDTO dto);

    /**
     * 修改份数：减到 0 视为移除
     */
    void updateQuantity(CartAddDTO dto);

    /**
     * 移除某个菜品
     */
    void removeItem(Long userId, Long recipeId);

    /**
     * 清空购物车
     */
    void clear(Long userId);

    /**
     * 购物车条目数量
     */
    int count(Long userId);
}
