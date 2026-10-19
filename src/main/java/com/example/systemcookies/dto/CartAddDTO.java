package com.example.systemcookies.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 加入购物车 / 修改份数入参
 */
@Data
public class CartAddDTO {

    /** 用户ID，演示阶段默认 1 */
    private Long userId = 1L;

    /** 食谱ID */
    @NotNull(message = "请选择菜品")
    private Long recipeId;

    /** 份数，默认 1 */
    @Min(value = 1, message = "份数至少为 1")
    private Integer quantity = 1;
}
