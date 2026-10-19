package com.example.systemcookies.controller;

import com.example.systemcookies.common.PageResult;
import com.example.systemcookies.common.R;
import com.example.systemcookies.common.UserContext;
import com.example.systemcookies.dto.OrderCreateDTO;
import com.example.systemcookies.dto.OrderQueryDTO;
import com.example.systemcookies.service.OrderService;
import com.example.systemcookies.vo.OrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 顾客端订单接口：只能操作自己的订单
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 提交订单
     */
    @PostMapping
    public R<Map<String, Long>> create(@Valid @RequestBody OrderCreateDTO dto) {
        dto.setUserId(UserContext.requiredUserId());
        Long id = orderService.createOrder(dto);
        return R.ok("下单成功", Map.of("id", id));
    }

    /**
     * 我的订单
     */
    @GetMapping
    public R<PageResult<OrderVO>> list(@ModelAttribute OrderQueryDTO query) {
        query.setUserId(UserContext.requiredUserId());
        return R.ok(orderService.pageQuery(query));
    }

    /**
     * 订单详情
     */
    @GetMapping("/{id}")
    public R<OrderVO> detail(@PathVariable Long id) {
        return R.ok(orderService.getDetail(id, UserContext.requiredUserId()));
    }

    /**
     * 取消订单
     */
    @PutMapping("/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id) {
        orderService.cancelOrder(id, UserContext.requiredUserId());
        return R.ok();
    }
}
