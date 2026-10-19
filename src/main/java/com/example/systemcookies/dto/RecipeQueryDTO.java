package com.example.systemcookies.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 食谱查询条件（顾客端 / 管理端共用）
 */
@Data
public class RecipeQueryDTO {

    /** 当前页，默认 1 */
    private Long pageNum = 1L;

    /** 每页条数，默认 10 */
    private Long pageSize = 10L;

    /** 关键字：菜名 / 副标题 / 简介 / 菜系 */
    private String keyword;

    /** 分类ID */
    private Long categoryId;

    /** 口味 */
    private String taste;

    /** 烹饪方法 */
    private String cookMethod;

    /** 难度：1简单 2中等 3困难 */
    private Integer difficulty;

    /** 最低价 */
    private BigDecimal minPrice;

    /** 最高价 */
    private BigDecimal maxPrice;

    /** 是否只看推荐 */
    private Boolean recommendOnly;

    /** 是否只看上架（管理端可传 false） */
    private Boolean onSaleOnly = true;

    /**
     * 排序方式：default / sales / priceAsc / priceDesc / time / newest
     */
    private String orderBy = "default";
}
