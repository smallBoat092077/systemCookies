<template>
  <div class="recipe-list-page">
    <van-nav-bar title="菜谱" fixed placeholder />

    <van-search
      v-model="keyword"
      shape="round"
      placeholder="搜索菜名 / 食材 / 菜系"
      @search="reload"
    />

    <van-tabs v-model:active="activeCategory" sticky @change="reload">
      <van-tab :name="0" title="全部" />
      <van-tab v-for="item in categories" :key="item.id" :name="item.id" :title="item.name" />
    </van-tabs>

    <van-dropdown-menu active-color="#ff7a45">
      <van-dropdown-item v-model="orderBy" :options="orderOptions" />
      <van-dropdown-item v-model="recommendOnly" :options="recommendOptions" />
    </van-dropdown-menu>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        :error="loadError"
        finished-text="没有更多菜谱了"
        error-text="加载失败，点击重试"
        @load="onLoad"
      >
        <RecipeCard
          v-for="item in list"
          :key="item.id"
          :recipe="item"
          @click="goDetail"
          @add="onAddCart"
        />
        <van-empty v-if="finished && !list.length" description="没有找到相关菜谱" />
      </van-list>
    </van-pull-refresh>

    <!-- 点菜小助理入口 -->
    <div class="assistant-fab" @click="showAssistant = true">
      <span class="fab-icon">🤖</span>
      <span class="fab-text">小助理</span>
    </div>

    <AssistantChat v-model:show="showAssistant" />
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import RecipeCard from '@/components/RecipeCard.vue'
import AssistantChat from '@/components/AssistantChat.vue'
import { showToast } from 'vant'
import { fetchCategories, fetchRecipePage } from '@/api/recipe'
import { addCart } from '@/api/cart'
import { isLogin } from '@/api/auth'

const router = useRouter()
const PAGE_SIZE = 6

const categories = ref([])
const list = ref([])
const total = ref(0)
const pageNum = ref(1)

const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const loadError = ref(false)

const keyword = ref('')
const activeCategory = ref(0)
const orderBy = ref('default')
const recommendOnly = ref(false)

const orderOptions = [
  { text: '综合排序', value: 'default' },
  { text: '销量优先', value: 'sales' },
  { text: '价格从低到高', value: 'priceAsc' },
  { text: '价格从高到低', value: 'priceDesc' },
  { text: '耗时最短', value: 'time' },
  { text: '最新上架', value: 'newest' }
]

const recommendOptions = [
  { text: '全部菜谱', value: false },
  { text: '只看招牌推荐', value: true }
]

const buildParams = () => ({
  pageNum: pageNum.value,
  pageSize: PAGE_SIZE,
  keyword: keyword.value || undefined,
  categoryId: activeCategory.value || undefined,
  orderBy: orderBy.value,
  recommendOnly: recommendOnly.value || undefined
})

const getList = async () => {
  try {
    const data = await fetchRecipePage(buildParams())
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

const goDetail = (recipe) => {
  router.push(`/recipe/${recipe.id}`)
}

/** 列表页快捷加购：未登录先引导登录 */
const onAddCart = async (recipe) => {
  if (!isLogin()) {
    showToast('请先登录')
    router.push({ path: '/login', query: { redirect: '/' } })
    return
  }
  await addCart({ recipeId: recipe.id, quantity: 1 })
  showToast(`已加入「${recipe.name}」`)
}

/** 点菜小助理弹层 */
const showAssistant = ref(false)

watch([orderBy, recommendOnly], reload)

onMounted(async () => {
  try {
    categories.value = await fetchCategories()
  } catch (e) {
    categories.value = []
  }
})
</script>

<style scoped>
.recipe-list-page {
  padding-bottom: 24px;
}

/* 悬浮在 TabBar 之上，避免遮挡 */
.assistant-fab {
  position: fixed;
  right: 16px;
  bottom: calc(50px + 16px);
  z-index: 20;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 14px;
  border-radius: 22px;
  background: linear-gradient(135deg, #ff9a6c, #ff7a45);
  color: #fff;
  box-shadow: 0 4px 12px rgba(255, 122, 69, 0.4);
}

.fab-icon {
  font-size: 18px;
}

.fab-text {
  font-size: 13px;
  font-weight: 600;
}
</style>
