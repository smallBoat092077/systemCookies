import request from './request'

/** 订单分页（不传 userId 表示全部用户） */
export const fetchOrderPage = (params) => request.get('/admin/orders', { params })

/** 订单详情 */
export const fetchOrderDetail = (id) => request.get(`/admin/orders/${id}`)

/** 推进订单状态：0待支付 1待制作 2制作中 3已完成 4已取消 */
export const updateOrderStatus = (id, status) =>
  request.put(`/admin/orders/${id}/status`, null, { params: { status } })
