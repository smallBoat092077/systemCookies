<template>
  <div class="order-list-page">
    <van-nav-bar title="我的订单" left-arrow fixed placeholder @click-left="onBack" />

    <van-tabs v-model:active="activeStatus" sticky @change="reload">
      <van-tab :name="0" title="全部" />
      <van-tab :name="1" title="待制作" />
      <van-tab :name="2" title="制作中" />
      <van-tab :name="3" title="已完成" />
      <van-tab :name="4" title="已取消" />
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        :error="loadError"
        finished-text="没有更多订单了"
        error-text="加载失败，点击重试"
        @load="onLoad"
      >
        <div v-for="order in list" :key="order.id" class="order-card">
          <div class="order-head">
            <span class="order-no">{{ order.orderNo }}</span>
            <van-tag :type="statusTagType(order.status)">{{ order.statusText }}</van-tag>
          </div>

          <div v-for="item in order.items" :key="item.id" class="order-dish">
            <span class="dish-name">{{ item.recipeName }}</span>
            <span class="dish-qty">× {{ item.quantity }}</span>
            <span class="dish-price">¥{{ item.price }}</span>
          </div>

          <div v-if="order.remark" class="order-remark">备注：{{ order.remark }}</div>

          <div class="order-foot">
            <span class="order-time">{{ order.createTime }}</span>
            <span class="order-total">
              共 {{ order.itemCount }} 份 · 合计
              <span class="price">¥{{ order.payAmount }}</span>
            </span>
          </div>

          <div v-if="order.status < 3" class="order-actions">
            <van-button size="small" plain type="danger" @click="onCancel(order)">
              取消订单
            </van-button>
          </div>
        </div>

        <van-empty v-if="finished && !list.length" description="还没有订单" />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { fetchOrderPage, cancelOrder } from '@/api/order'

const router = useRouter()
const PAGE_SIZE = 10

const list = ref([])
const total = ref(0)
const pageNum = ref(1)

const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const loadError = ref(false)

const activeStatus = ref(0)

const statusTagType = (status) => {
  if (status === 3) return 'success'
  if (status === 4) return 'default'
  if (status === 2) return 'warning'
  return 'primary'
}

const getList = async () => {
  try {
    const data = await fetchOrderPage({
      pageNum: pageNum.value,
      pageSize: PAGE_SIZE,
      status: activeStatus.value === 0 ? undefined : activeStatus.value
    })
    list.value = pageNum.value === 1 ? data.records : list.value.concat(data.records)
    total.value = data.total
    pageNum.value += 1
    finished.value = list.value.length >= data.total
    loadError.value = false
  } catch (e) {
    loadError.value = true
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const onLoad = () => {
  getList()
}

const reload = () => {
  pageNum.value = 1
  finished.value = false
  loading.value = true
  getList()
}

const onRefresh = () => {
  refreshing.value = true
  reload()
}

const onCancel = async (order) => {
  try {
    await showConfirmDialog({
      title: '取消订单',
      message: '取消后库存会自动归还，确定取消吗？'
    })
  } catch (e) {
    return
  }
  await cancelOrder(order.id)
  showToast('订单已取消')
  reload()
}

const onBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.replace('/')
  }
}

onMounted(reload)
</script>

<style scoped>
.order-list-page {
  padding-bottom: 24px;
}

.order-card {
  padding: 12px;
  margin: 12px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.order-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 8px;
  border-bottom: 1px solid #f2f3f5;
}

.order-no {
  font-size: 13px;
  color: #646566;
}

.order-dish {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  font-size: 14px;
}

.dish-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dish-qty {
  color: #969799;
  font-size: 13px;
}

.dish-price {
  color: #323233;
}

.order-remark {
  padding: 6px 8px;
  margin-top: 4px;
  border-radius: 6px;
  background: #fff7e8;
  font-size: 12px;
  color: #b26a00;
}

.order-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: #969799;
}

.price {
  font-size: 15px;
  font-weight: 600;
  color: #ee0a24;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}
</style>
