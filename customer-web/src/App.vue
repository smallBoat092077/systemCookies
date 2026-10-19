<template>
  <div class="app-shell">
    <router-view />
    <van-tabbar route placeholder active-color="#ff7a45">
      <van-tabbar-item to="/" icon="shop-o">菜谱</van-tabbar-item>
      <van-tabbar-item to="/cart" icon="cart-o" :badge="cartCount > 0 ? String(cartCount) : ''">
        购物车
      </van-tabbar-item>
      <van-tabbar-item to="/check-in" icon="photo-o">打卡</van-tabbar-item>
      <van-tabbar-item to="/orders" icon="orders-o">订单</van-tabbar-item>
      <van-tabbar-item to="/mine" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { fetchCartCount } from '@/api/cart'
import { clearAuth, isLogin } from '@/api/auth'

const route = useRoute()
const cartCount = ref(0)

const loadCartCount = async () => {
  // 未登录时购物车接口会返回 401，这里直接跳过
  if (!isLogin()) {
    cartCount.value = 0
    return
  }
  try {
    const data = await fetchCartCount()
    cartCount.value = data.count || 0
  } catch (e) {
    cartCount.value = 0
  }
}

// 路由变化时刷新角标，加入购物车后进入购物车/订单页即可看到最新数量
watch(() => route.fullPath, loadCartCount)

// 令牌失效被 request 拦截清理时，同步清空全局登录态
onMounted(() => {
  window.addEventListener('auth:logout', clearAuth)
  // 点菜小助理加购/下单成功后刷新角标
  window.addEventListener('cart:changed', loadCartCount)
  loadCartCount()
})

onUnmounted(() => {
  window.removeEventListener('auth:logout', clearAuth)
  window.removeEventListener('cart:changed', loadCartCount)
})
</script>
