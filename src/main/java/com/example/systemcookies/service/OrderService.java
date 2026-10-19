package com.example.systemcookies.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.example.systemcookies.common.PageResult;
import com.example.systemcookies.dto.OrderCreateDTO;
import com.example.systemcookies.dto.OrderQueryDTO;
import com.example.systemcookies.entity.Order;
import com.example.systemcookies.vo.OrderVO;

/**
 * 订单服务：下单（扣库存加销量）/ 取消（归还库存）/ 状态流转
 */
public interface OrderService extends IService<Order> {

    /**
     * 下单
     *
     * @return 订单ID
     */
    Long createOrder(OrderCreateDTO dto);

    /**
     * 订单详情，userId 非空时校验归属
     */
    OrderVO getDetail(Long orderId, Long userId);

    /**
     * 取消订单（仅未完成订单可取消，自动归还库存）
     */
    void cancelOrder(Long orderId, Long userId);

    /**
     * 订单分页
     */
    PageResult<OrderVO> pageQuery(OrderQueryDTO query);

    /**
     * 管理端推进订单状态
     */
    void updateStatus(Long orderId, Integer status);
}
