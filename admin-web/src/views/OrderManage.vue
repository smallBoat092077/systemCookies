<template>
  <div class="page">
    <el-card shadow="never">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="订单号">
          <el-input
            v-model="query.keyword"
            placeholder="支持模糊查询"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 12px">
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="orderNo" label="订单号" min-width="180" show-overflow-tooltip />
        <el-table-column label="联系人" width="140">
          <template #default="{ row }">
            <span>{{ row.contactName || '-' }}</span>
            <div v-if="row.contactPhone" class="sub-text">{{ row.contactPhone }}</div>
          </template>
        </el-table-column>
        <el-table-column label="菜品" min-width="220">
          <template #default="{ row }">
            <span v-for="item in row.items" :key="item.id" class="dish">
              {{ item.recipeName }} × {{ item.quantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="itemCount" label="份数" width="70" align="center" />
        <el-table-column label="金额" width="110">
          <template #default="{ row }">
            <span class="price">¥{{ row.payAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ row.remark || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 1"
              link
              type="warning"
              @click="changeStatus(row, 2)"
            >
              开始制作
            </el-button>
            <el-button
              v-if="row.status === 2"
              link
              type="success"
              @click="changeStatus(row, 3)"
            >
              完成
            </el-button>
            <el-button
              v-if="row.status < 3"
              link
              type="danger"
              @click="changeStatus(row, 4)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无订单" />
        </template>
      </el-table>

      <el-pagination
        style="margin-top: 16px; justify-content: flex-end"
        layout="total, sizes, prev, pager, next"
        :total="total"
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :page-sizes="[10, 20, 50]"
        @current-change="getList"
        @size-change="handleSearch"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="订单详情" width="640px">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="订单号">{{ current.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(current.status)" size="small">{{ current.statusText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="联系人">{{ current.contactName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ current.contactPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ current.address || '堂食 / 自取' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ current.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ current.createTime }}</el-descriptions-item>
        <el-descriptions-item label="合计">
          <span class="price">¥{{ current.payAmount }}</span>
        </el-descriptions-item>
      </el-descriptions>

      <el-table :data="current.items || []" size="small" style="margin-top: 12px" border>
        <el-table-column prop="recipeName" label="菜名" min-width="160" />
        <el-table-column prop="price" label="单价" width="90" />
        <el-table-column prop="quantity" label="份数" width="70" align="center" />
        <el-table-column label="小计" width="100">
          <template #default="{ row }">¥{{ (row.price * row.quantity).toFixed(2) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchOrderPage, fetchOrderDetail, updateOrderStatus } from '@/api/order'

const statusOptions = [
  { label: '待支付', value: 0 },
  { label: '待制作', value: 1 },
  { label: '制作中', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 }
]

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  status: undefined
})

const list = ref([])
const total = ref(0)
const loading = ref(false)
const detailVisible = ref(false)
const current = ref({})

const statusTagType = (status) => {
  if (status === 3) return 'success'
  if (status === 4) return 'info'
  if (status === 2) return 'warning'
  return 'primary'
}

const getList = async () => {
  loading.value = true
  try {
    const data = await fetchOrderPage({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      status: query.status
    })
    list.value = data.records || []
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.pageNum = 1
  getList()
}

const handleReset = () => {
  query.keyword = ''
  query.status = undefined
  handleSearch()
}

const openDetail = async (row) => {
  current.value = await fetchOrderDetail(row.id)
  detailVisible.value = true
}

const changeStatus = async (row, status) => {
  const text = statusOptions.find((item) => item.value === status)?.label || ''
  if (status === 4) {
    try {
      await ElMessageBox.confirm('取消后会自动归还库存，确定取消该订单吗？', '取消订单', {
        type: 'warning',
        confirmButtonText: '确定取消',
        cancelButtonText: '返回'
      })
    } catch (e) {
      return
    }
  }
  await updateOrderStatus(row.id, status)
  ElMessage.success(`已更新为「${text}」`)
  getList()
}

onMounted(getList)
</script>

<style scoped>
.price {
  color: #f56c6c;
  font-weight: 600;
}

.sub-text {
  font-size: 12px;
  color: #909399;
}

.dish {
  display: inline-block;
  margin-right: 10px;
  padding: 2px 8px;
  border-radius: 4px;
  background: #f4f4f5;
  font-size: 12px;
  line-height: 20px;
}
</style>
