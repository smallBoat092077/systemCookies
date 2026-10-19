<template>
  <div class="page">
    <el-card shadow="never">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="关键字">
          <el-input
            v-model="query.keyword"
            placeholder="菜名 / 副标题 / 简介"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width: 160px">
            <el-option
              v-for="item in categories"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="已上架" value="1" />
            <el-option label="已下架" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="goCreate">新增菜谱</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 12px">
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column label="封面" width="80" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.coverUrl"
              :src="row.coverUrl"
              fit="cover"
              style="width: 48px; height: 48px; border-radius: 6px"
            >
              <template #error>
                <div class="cover-fallback">{{ row.name?.slice(0, 1) }}</div>
              </template>
            </el-image>
            <div v-else class="cover-fallback">{{ row.name?.slice(0, 1) }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="菜名" min-width="160" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column label="价格" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
            <span v-if="row.originalPrice" class="origin">¥{{ row.originalPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="cookTime" label="耗时" width="90">
          <template #default="{ row }">{{ row.cookTime }} 分钟</template>
        </el-table-column>
        <el-table-column prop="stepCount" label="步骤" width="80" align="center" />
        <el-table-column prop="stock" label="库存" width="80" align="center" />
        <el-table-column prop="sales" label="销量" width="80" align="center" />
        <el-table-column label="招牌" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isRecommend === 1" type="danger" size="small">招牌</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="上架状态" width="110" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(val) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无菜谱" />
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchRecipePage, deleteRecipe, updateRecipeStatus } from '@/api/recipe'
import { fetchCategoryList } from '@/api/category'

const router = useRouter()

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  categoryId: undefined,
  status: ''
})

const list = ref([])
const total = ref(0)
const loading = ref(false)
const categories = ref([])

const buildParams = () => ({
  pageNum: query.pageNum,
  pageSize: query.pageSize,
  keyword: query.keyword || undefined,
  categoryId: query.categoryId || undefined,
  // 管理端按需过滤上架状态；不传 status 时由后端返回全部
  onSaleOnly: query.status === '' ? false : undefined,
  status: undefined
})

const getList = async () => {
  loading.value = true
  try {
    const params = buildParams()
    const data = await fetchRecipePage(params)
    const rows = data.records || []
    list.value =
      query.status === ''
        ? rows
        : rows.filter((row) => String(row.status) === String(query.status))
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
  query.categoryId = undefined
  query.status = ''
  handleSearch()
}

const handleStatusChange = async (row, val) => {
  try {
    await updateRecipeStatus(row.id, val)
    ElMessage.success(val === 1 ? '已上架' : '已下架')
  } catch (e) {
    row.status = val === 1 ? 0 : 1
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除菜谱「${row.name}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }
  await deleteRecipe(row.id)
  ElMessage.success('删除成功')
  getList()
}

const goCreate = () => router.push('/recipes/create')
const goEdit = (row) => router.push(`/recipes/edit/${row.id}`)

onMounted(async () => {
  getList()
  try {
    categories.value = await fetchCategoryList()
  } catch (e) {
    categories.value = []
  }
})
</script>

<style scoped>
.price {
  color: #f56c6c;
  font-weight: 600;
}

.origin {
  margin-left: 6px;
  color: #c0c4cc;
  font-size: 12px;
  text-decoration: line-through;
}

.cover-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  margin: 0 auto;
  border-radius: 6px;
  color: #fff;
  font-weight: 600;
  background: linear-gradient(135deg, #ff9a6c, #ff7a45);
}
</style>
