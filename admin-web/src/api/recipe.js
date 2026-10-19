import request from './request'

/** 食谱分页（管理端可看下架菜品） */
export const fetchRecipePage = (params) => request.get('/admin/recipes', { params })

/** 食谱详情（含下架，供编辑回填） */
export const fetchRecipeDetail = (id) => request.get(`/admin/recipes/${id}`)

/** 新增食谱：返回 { id } */
export const createRecipe = (data) => request.post('/admin/recipes', data)

/** 修改食谱（子表全量替换） */
export const updateRecipe = (data) => request.put('/admin/recipes', data)

/** 删除食谱 */
export const deleteRecipe = (id) => request.delete(`/admin/recipes/${id}`)

/** 上架 / 下架 */
export const updateRecipeStatus = (id, status) =>
  request.put(`/admin/recipes/${id}/status`, null, { params: { status } })
