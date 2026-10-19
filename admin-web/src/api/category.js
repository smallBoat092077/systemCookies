import request from './request'

/** 分类列表（含禁用） */
export const fetchCategoryList = () => request.get('/admin/categories')

/** 新增分类：返回 { id } */
export const createCategory = (data) => request.post('/admin/categories', data)

/** 修改分类 */
export const updateCategory = (data) => request.put('/admin/categories', data)

/** 删除分类 */
export const deleteCategory = (id) => request.delete(`/admin/categories/${id}`)
