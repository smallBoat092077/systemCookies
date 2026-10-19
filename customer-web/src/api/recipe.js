import request from './request'

/** 食谱分类（启用中的） */
export const fetchCategories = () => request.get('/recipes/categories')

/** 食谱分页列表 */
export const fetchRecipePage = (params) => request.get('/recipes', { params })

/** 推荐食谱 */
export const fetchRecommend = (limit = 6) => request.get('/recipes/recommend', { params: { limit } })

/** 食谱详情：基本信息 + 步骤 + 食材 + 调料 + 技巧 */
export const fetchRecipeDetail = (id) => request.get(`/recipes/${id}`)

/** 只看烹饪步骤 */
export const fetchRecipeSteps = (id) => request.get(`/recipes/${id}/steps`)
