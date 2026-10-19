package com.example.systemcookies.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 食谱新增 / 修改入参（含烹饪步骤、食材、调料、技巧四类子表）
 */
@Data
public class RecipeSaveDTO {

    /** 主键，修改时必填 */
    private Long id;

    /** 所属分类ID */
    private Long categoryId;

    @NotBlank(message = "食谱名称不能为空")
    private String name;

    private String subtitle;

    private String coverUrl;

    private String description;

    /** 菜系 */
    private String cuisine;

    /** 口味 */
    private String taste;

    /** 主要烹饪方法 */
    private String cookMethod;

    /** 难度：1简单 2中等 3困难 */
    @Min(value = 1, message = "难度取值非法")
    private Integer difficulty = 1;

    /** 预计总耗时（分钟） */
    @Min(value = 0, message = "耗时不能为负")
    private Integer cookTime = 0;

    /** 份量 */
    @Min(value = 1, message = "份量至少为1")
    private Integer servings = 1;

    private Integer calories;

    @NotNull(message = "点菜价格不能为空")
    private BigDecimal price;

    private BigDecimal originalPrice;

    @Min(value = 0, message = "库存不能为负")
    private Integer stock = 0;

    /** 是否推荐 */
    private Integer isRecommend = 0;

    /** 状态：1上架 0下架 */
    private Integer status = 1;

    /** 烹饪步骤 */
    @Valid
    private List<StepItem> steps = new ArrayList<>();

    /** 食材清单 */
    @Valid
    private List<IngredientItem> ingredients = new ArrayList<>();

    /** 调料清单 */
    @Valid
    private List<SeasoningItem> seasonings = new ArrayList<>();

    /** 烹饪技巧 */
    @Valid
    private List<TipItem> tips = new ArrayList<>();

    @Data
    public static class StepItem {
        @NotNull(message = "步骤序号不能为空")
        private Integer stepNo;
        private String title;
        @NotBlank(message = "步骤内容不能为空")
        private String content;
        private String imageUrl;
        private Integer duration;
        private String tip;
    }

    @Data
    public static class IngredientItem {
        @NotBlank(message = "食材名称不能为空")
        private String name;
        private String amount;
        private String unit;
        private Integer isMain;
        private String note;
        private Integer sort;
    }

    @Data
    public static class SeasoningItem {
        @NotBlank(message = "调料名称不能为空")
        private String name;
        private String amount;
        private String unit;
        private String note;
        private Integer sort;
    }

    @Data
    public static class TipItem {
        @NotBlank(message = "技巧标题不能为空")
        private String title;
        private String content;
        private Integer sort;
    }
}
