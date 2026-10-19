import { createRouter, createWebHistory } from 'vue-router'
import { isLogin } from '@/api/auth'

const routes = [
  {
    path: '/',
    name: 'recipe-list',
    component: () => import('@/views/RecipeList.vue'),
    meta: { title: '菜谱' }
  },
  {
    path: '/recipe/:id',
    name: 'recipe-detail',
    component: () => import('@/views/RecipeDetail.vue'),
    meta: { title: '菜谱详情' }
  },
  {
    path: '/cart',
    name: 'cart',
    component: () => import('@/views/Cart.vue'),
    meta: { title: '购物车', requiresAuth: true }
  },
  {
    path: '/order-confirm',
    name: 'order-confirm',
    component: () => import('@/views/OrderConfirm.vue'),
    meta: { title: '确认下单', requiresAuth: true }
  },
  {
    path: '/orders',
    name: 'order-list',
    component: () => import('@/views/OrderList.vue'),
    meta: { title: '我的订单', requiresAuth: true }
  },
  {
    path: '/check-in',
    name: 'check-in',
    component: () => import('@/views/CheckIn.vue'),
    meta: { title: '今日打卡', requiresAuth: true }
  },
  {
    path: '/mine',
    name: 'mine',
    component: () => import('@/views/Mine.vue'),
    meta: { title: '我的' }
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录 / 注册' }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

// 需要登录的页面：未登录先去登录页，登录后回跳
router.beforeEach((to) => {
  if (to.meta?.requiresAuth && !isLogin()) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

router.afterEach((to) => {
  if (to.meta?.title) {
    document.title = to.meta.title
  }
})

export default router
