package com.example.systemcookies.vo;

import com.example.systemcookies.entity.Order;
import com.example.systemcookies.entity.OrderItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 订单详情：订单信息 + 订单项 + 状态文案
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderVO extends Order {

    /** 订单项 */
    private List<OrderItem> items = List.of();

    /** 状态文案 */
    private String statusText;
}
