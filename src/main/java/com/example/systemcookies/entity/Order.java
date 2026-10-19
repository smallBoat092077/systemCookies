package com.example.systemcookies.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.systemcookies.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("orders")
public class Order extends BaseEntity {

    /** 订单号 */
    private String orderNo;

    /** 下单用户ID */
    private Long userId;

    /** 订单总额 */
    private BigDecimal totalAmount;

    /** 应付金额 */
    private BigDecimal payAmount;

    /** 菜品总份数 */
    private Integer itemCount;

    /** 订单状态：0待支付 1已下单待制作 2制作中 3已完成 4已取消 */
    private Integer status;

    /** 联系人 */
    private String contactName;

    /** 联系电话 */
    private String contactPhone;

    /** 配送地址，堂食可为空 */
    private String address;

    /** 备注：口味、忌口等 */
    private String remark;
}
