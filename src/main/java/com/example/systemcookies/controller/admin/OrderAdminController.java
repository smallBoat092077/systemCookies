package com.example.systemcookies.controller.admin;

import com.example.systemcookies.common.PageResult;
import com.example.systemcookies.common.R;
import com.example.systemcookies.dto.OrderQueryDTO;
import com.example.systemcookies.service.OrderService;
import com.example.systemcookies.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端订单接口：查看全部订单并推进状态
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class OrderAdminController {

    private final OrderService orderService;

    /**
     * 订单分页（不传 userId 表示全部用户）
     */
    @GetMapping
    public R<PageResult<OrderVO>> list(@ModelAttribute OrderQueryDTO query) {
        return R.ok(orderService.pageQuery(query));
    }

    /**
     * 订单详情
     */
    @GetMapping("/{id}")
    public R<OrderVO> detail(@PathVariable Long id) {
        return R.ok(orderService.getDetail(id, null));
    }

    /**
     * 推进订单状态：0待支付 1待制作 2制作中 3已完成 4已取消
     */
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        orderService.updateStatus(id, status);
        return R.ok();
    }
}
