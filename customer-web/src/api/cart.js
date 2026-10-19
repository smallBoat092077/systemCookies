import request from './request'

/** 购物车列表：用户身份由登录令牌决定 */
export const fetchCartList = () => request.get('/cart')

/** 加入购物车 */
export const addCart = (data) => request.post('/cart', data)

/** 修改份数，quantity 传 0 表示移除 */
export const updateCartQuantity = (recipeId, quantity) =>
  request.put(`/cart/${recipeId}`, null, { params: { quantity } })

/** 移除某个菜品 */
export const removeCartItem = (recipeId) => request.delete(`/cart/${recipeId}`)

/** 清空购物车 */
export const clearCart = () => request.delete('/cart')

/** 购物车条目数量 */
export const fetchCartCount = () => request.get('/cart/count')
