import request from './request'

/** 提交订单，payload 支持 cartItemIds（购物车结算）或 items（立即购买） */
export const createOrder = (payload) => request.post('/orders', payload)

/** 我的订单 */
export const fetchOrderPage = (params) => request.get('/orders', { params })

/** 订单详情 */
export const fetchOrderDetail = (id) => request.get(`/orders/${id}`)

/** 取消订单 */
export const cancelOrder = (id) => request.put(`/orders/${id}/cancel`)
