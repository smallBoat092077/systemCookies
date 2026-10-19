package com.example.systemcookies.dto;

import lombok.Data;

/**
 * 订单查询条件：顾客端按用户查，管理端可按状态与订单号查
 */
@Data
public class OrderQueryDTO {

    /** 当前页，默认 1 */
    private Long pageNum = 1L;

    /** 每页条数，默认 10 */
    private Long pageSize = 10L;

    /** 用户ID，管理端不传表示查全部 */
    private Long userId;

    /** 订单状态 */
    private Integer status;

    /** 订单号关键字 */
    private String keyword;
}
