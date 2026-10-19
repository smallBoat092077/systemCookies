<template>
  <div class="confirm-page">
    <van-nav-bar title="确认下单" left-arrow fixed placeholder @click-left="onBack" />

    <van-loading v-if="loading" vertical class="page-loading">加载中...</van-loading>

    <template v-else>
      <van-cell-group inset title="配送信息">
        <van-field v-model="form.contactName" label="联系人" placeholder="如：徐先生" />
        <van-field v-model="form.contactPhone" label="联系电话" placeholder="手机号" type="tel" />
        <van-field v-model="form.address" label="地址" placeholder="堂食可留空" />
        <van-field
          v-model="form.remark"
          label="备注"
          placeholder="口味、忌口等要求"
          type="textarea"
          rows="2"
        />
      </van-cell-group>

      <van-cell-group inset title="菜品清单">
        <van-cell
          v-for="item in dishes"
          :key="item.recipeId"
          :title="item.name"
          :value="`¥${item.price} × ${item.quantity}`"
          :label="`小计 ¥${item.subtotal}`"
        />
      </van-cell-group>

      <van-submit-bar
        :price="totalPrice"
        :loading="submitting"
        button-text="提交订单"
        @submit="onSubmit"
      />
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { fetchCartList } from '@/api/cart'
import { fetchRecipeDetail } from '@/api/recipe'
import { createOrder } from '@/api/order'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const submitting = ref(false)
const dishes = ref([])

const form = reactive({
  contactName: '',
  contactPhone: '',
  address: '',
  remark: ''
})

/** 购物车结算：/order-confirm?ids=1,2 */
const cartIds = computed(() =>
  route.query.ids ? String(route.query.ids).split(',').map(Number).filter(Boolean) : []
)

/** 立即购买：/order-confirm?buy=1&qty=2 */
const buyRecipeId = computed(() => (route.query.buy ? Number(route.query.buy) : null))
const buyQuantity = computed(() => {
  const qty = Number(route.query.qty)
  return Number.isFinite(qty) && qty > 0 ? qty : 1
})

const totalAmount = computed(() =>
  dishes.value.reduce((sum, item) => sum + Number(item.price) * item.quantity, 0)
)
const totalPrice = computed(() => Math.round(totalAmount.value * 100))

const loadDishes = async () => {
  if (buyRecipeId.value) {
    const detail = await fetchRecipeDetail(buyRecipeId.value)
    dishes.value = [
      {
        recipeId: detail.id,
        name: detail.name,
        price: detail.price,
        quantity: buyQuantity.value,
        subtotal: (Number(detail.price) * buyQuantity.value).toFixed(2)
      }
    ]
    return
  }
  const cart = await fetchCartList()
  dishes.value = cart
    .filter((item) => cartIds.value.includes(item.id))
    .map((item) => ({
      recipeId: item.recipeId,
      name: item.name,
      price: item.price,
      quantity: item.quantity,
      subtotal: item.subtotal
    }))
}

const onSubmit = async () => {
  if (!dishes.value.length) {
    showToast('没有可结算的菜品')
    return
  }
  submitting.value = true
  try {
    const payload = {
      contactName: form.contactName,
      contactPhone: form.contactPhone,
      address: form.address,
      remark: form.remark
    }
    if (buyRecipeId.value) {
      payload.items = [{ recipeId: buyRecipeId.value, quantity: buyQuantity.value }]
    } else {
      payload.cartItemIds = cartIds.value
    }
    await createOrder(payload)
    showToast('下单成功，厨房已收到')
    router.replace('/orders')
  } finally {
    submitting.value = false
  }
}

const onBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.replace('/')
  }
}

onMounted(async () => {
  try {
    await loadDishes()
  } catch (e) {
    dishes.value = []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.confirm-page {
  padding-bottom: 160px;
  min-height: 100vh;
}

.page-loading {
  padding: 60px 0;
}
</style>
