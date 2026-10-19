<template>
  <div class="cart-page">
    <van-nav-bar title="购物车" left-arrow fixed placeholder @click-left="onBack" />

    <van-empty v-if="!list.length" description="购物车还是空的，去挑几道菜吧">
      <van-button round type="primary" to="/">去点菜</van-button>
    </van-empty>

    <template v-else>
      <van-swipe-cell v-for="item in list" :key="item.id">
        <div class="cart-item">
          <van-checkbox
            v-model="checkedMap[item.id]"
            :disabled="!item.available"
            class="check"
          />
          <div class="cover-fallback small">{{ item.name?.slice(0, 1) }}</div>
          <div class="info">
            <div class="name">{{ item.name }}</div>
            <div v-if="item.subtitle" class="subtitle">{{ item.subtitle }}</div>
            <div class="price-row">
              <span class="price">¥{{ item.price }}</span>
              <van-tag v-if="item.status !== 1" type="danger">已下架</van-tag>
              <van-tag v-else-if="item.stock < item.quantity" type="warning">
                仅剩 {{ item.stock }} 份
              </van-tag>
            </div>
            <div class="qty-row">
              <van-stepper
                v-model="item.quantity"
                :min="1"
                :max="item.stock || 1"
                integer
                @change="() => onQuantityChange(item)"
              />
              <span class="subtotal">小计 ¥{{ item.subtotal }}</span>
            </div>
          </div>
        </div>
        <template #right>
          <van-button square type="danger" text="删除" class="swipe-btn" @click="onRemove(item)" />
        </template>
      </van-swipe-cell>

      <van-submit-bar
        :price="totalPrice"
        button-text="去结算"
        :disabled="!selectedIds.length"
        @submit="onSubmit"
      >
        <van-checkbox v-model="allChecked">全选</van-checkbox>
        <template #tip>
          共选中 {{ selectedIds.length }} 件，已下架或库存不足的菜品不可结算
        </template>
      </van-submit-bar>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import {
  fetchCartList,
  updateCartQuantity,
  removeCartItem
} from '@/api/cart'

const router = useRouter()

const list = ref([])
const checkedMap = reactive({})

const availableList = computed(() => list.value.filter((item) => item.available))

const allChecked = computed({
  get: () =>
    availableList.value.length > 0 && availableList.value.every((item) => checkedMap[item.id]),
  set: (val) => availableList.value.forEach((item) => (checkedMap[item.id] = val))
})

const selectedIds = computed(() =>
  list.value.filter((item) => item.available && checkedMap[item.id]).map((item) => item.id)
)

// van-submit-bar 的 price 单位是「分」
const totalPrice = computed(() =>
  Math.round(
    list.value
      .filter((item) => item.available && checkedMap[item.id])
      .reduce((sum, item) => sum + Number(item.price) * item.quantity, 0) * 100
  )
)

const load = async () => {
  const data = await fetchCartList()
  list.value = data
  data.forEach((item) => {
    if (checkedMap[item.id] === undefined) {
      checkedMap[item.id] = item.available
    }
  })
}

const onQuantityChange = async (item) => {
  try {
    await updateCartQuantity(item.recipeId, item.quantity)
    await load()
  } catch (e) {
    await load()
  }
}

const onRemove = async (item) => {
  await removeCartItem(item.recipeId)
  delete checkedMap[item.id]
  showToast('已移除')
  await load()
}

const onSubmit = () => {
  if (!selectedIds.value.length) {
    showToast('请先选择要结算的菜品')
    return
  }
  router.push({ path: '/order-confirm', query: { ids: selectedIds.value.join(',') } })
}

const onBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.replace('/')
  }
}

onMounted(load)
</script>

<style scoped>
.cart-page {
  padding-bottom: 160px;
  min-height: 100vh;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  margin: 0 12px 10px;
  background: #fff;
  border-radius: 12px;
}

.check {
  flex: none;
}

.cover-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ff9a6c, #ff7a45);
  color: #fff;
  font-weight: 600;
}

.cover-fallback.small {
  width: 64px;
  height: 64px;
  flex: none;
  border-radius: 8px;
  font-size: 24px;
}

.info {
  flex: 1;
  min-width: 0;
}

.name {
  font-size: 15px;
  font-weight: 600;
}

.subtitle {
  font-size: 12px;
  color: #969799;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 4px 0;
}

.price {
  color: #ee0a24;
  font-weight: 600;
}

.qty-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.subtotal {
  font-size: 13px;
  color: #ee0a24;
  font-weight: 600;
}

.swipe-btn {
  height: 100%;
}
</style>
