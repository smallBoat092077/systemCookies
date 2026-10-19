import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/recipes',
    children: [
      {
        path: 'recipes',
        name: 'recipe-list',
        component: () => import('@/views/RecipeList.vue'),
        meta: { title: '菜谱管理', icon: 'Dish' }
      },
      {
        path: 'recipes/create',
        name: 'recipe-create',
        component: () => import('@/views/RecipeEdit.vue'),
        meta: { title: '新增菜谱', hidden: true, activeMenu: '/recipes' }
      },
      {
        path: 'recipes/edit/:id',
        name: 'recipe-edit',
        component: () => import('@/views/RecipeEdit.vue'),
        meta: { title: '编辑菜谱', hidden: true, activeMenu: '/recipes' }
      },
      {
        path: 'orders',
        name: 'order-manage',
        component: () => import('@/views/OrderManage.vue'),
        meta: { title: '订单管理', icon: 'Tickets' }
      },
      {
        path: 'categories',
        name: 'category-manage',
        component: () => import('@/views/CategoryManage.vue'),
        meta: { title: '分类管理', icon: 'Menu' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 管理端整体需要登录：未登录一律跳登录页，登录后回跳原地址
router.beforeEach((to) => {
  if (to.path === '/login') {
    return true
  }
  if (!localStorage.getItem('token')) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

router.afterEach((to) => {
  document.title = to.meta?.title ? `${to.meta.title} · 食谱点菜管理后台` : '食谱点菜管理后台'
})

export default router
