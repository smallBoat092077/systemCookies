<template>
  <div class="checkin-page">
    <van-nav-bar title="今日打卡" left-arrow fixed placeholder @click-left="onBack" />

    <div class="today-card">
      <div class="today-num">{{ todayCount }}</div>
      <div class="today-label">今日已打卡</div>
    </div>

    <van-cell-group inset class="block">
      <van-field
        :model-value="selectedName"
        label="菜谱"
        placeholder="请选择要打卡的菜谱"
        readonly
        is-link
        @click="openPicker"
      />
    </van-cell-group>

    <van-cell-group inset class="block">
      <van-field
        v-model="content"
        type="textarea"
        rows="4"
        maxlength="500"
        show-word-limit
        placeholder="说说今天的成果：味道如何、有什么小改动…"
      />
    </van-cell-group>

    <van-cell-group inset class="block">
      <div class="block-title">成果图片（最多 9 张）</div>
      <div class="upload-box">
        <van-uploader
          v-model="fileList"
          multiple
          :max-count="9"
          :max-size="10 * 1024 * 1024"
          :after-read="afterRead"
          upload-icon="plus"
          @oversize="onOversize"
        />
      </div>
    </van-cell-group>

    <div class="submit-box">
      <van-button round block type="primary" :loading="submitting" @click="onSubmit">
        发布打卡
      </van-button>
    </div>

    <div class="block-title plain">我的打卡</div>
    <div v-if="mine.length" class="mine-list">
      <div v-for="item in mine" :key="item.id" class="mine-card">
        <div class="mine-head">
          <div class="mine-recipe">
            <van-tag type="primary" size="medium">{{ item.recipeName }}</van-tag>
          </div>
          <span class="mine-time">{{ item.createTime }}</span>
          <van-button size="mini" type="danger" plain @click="onRemove(item)">删除</van-button>
        </div>
        <div v-if="item.content" class="mine-text">{{ item.content }}</div>
        <div v-if="item.images.length" class="mine-images">
          <van-image
            v-for="(img, index) in item.images"
            :key="index"
            :src="img"
            fit="cover"
            class="mine-image"
            @click="preview(index, item.images)"
          />
        </div>
      </div>
    </div>
    <van-empty v-else description="还没有打卡记录，来做第一个吧" />

    <!-- 菜谱选择弹层 -->
    <van-popup v-model:show="showPicker" position="bottom" :style="{ height: '70%' }">
      <div class="picker-page">
        <van-search
          v-model="keyword"
          placeholder="搜索菜谱"
          @search="loadRecipes(true)"
          @update:model-value="onKeywordInput"
        />
        <van-list
          v-model:loading="pickerLoading"
          :finished="pickerFinished"
          finished-text="没有更多菜谱了"
          @load="loadRecipes(false)"
        >
          <van-cell
            v-for="item in recipeOptions"
            :key="item.id"
            :title="item.name"
            :label="`¥${item.price}`"
            clickable
            @click="onPick(item)"
          />
        </van-list>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showConfirmDialog, showImagePreview, showToast } from 'vant'
import { fetchRecipeDetail, fetchRecipePage } from '@/api/recipe'
import {
  createCheckIn,
  deleteCheckIn,
  fetchMyCheckIns,
  fetchTodayCount,
  uploadImage
} from '@/api/checkin'

const route = useRoute()
const router = useRouter()

const submitting = ref(false)
const content = ref('')
const fileList = ref([])
const selected = ref(null)
const selectedName = computed(() => (selected.value ? selected.value.name : ''))

const todayCount = ref(0)
const mine = ref([])

// 菜谱选择弹层
const showPicker = ref(false)
const keyword = ref('')
const recipeOptions = ref([])
const pickerPage = ref(0)
const pickerLoading = ref(false)
const pickerFinished = ref(false)
let searchTimer = null

const openPicker = () => {
  showPicker.value = true
  if (!recipeOptions.value.length) {
    loadRecipes(true)
  }
}

/** 弹层内菜谱分页，reset 为 true 时重新搜索 */
const loadRecipes = async (reset = false) => {
  if (reset) {
    pickerPage.value = 0
    pickerFinished.value = false
    recipeOptions.value = []
  }
  if (pickerLoading.value || pickerFinished.value) {
    return
  }
  pickerLoading.value = true
  try {
    const page = await fetchRecipePage({
      pageNum: pickerPage.value + 1,
      pageSize: 20,
      keyword: keyword.value || undefined
    })
    recipeOptions.value = recipeOptions.value.concat(page.records)
    pickerPage.value += 1
    pickerFinished.value = recipeOptions.value.length >= page.total
  } catch (e) {
    pickerFinished.value = true
  } finally {
    pickerLoading.value = false
  }
}

const onKeywordInput = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => loadRecipes(true), 300)
}

const onPick = (recipe) => {
  selected.value = recipe
  showPicker.value = false
}

/** 选完图片立即上传，拿到地址随打卡一起提交 */
const afterRead = async (file) => {
  const items = Array.isArray(file) ? file : [file]
  for (const item of items) {
    item.status = 'uploading'
    item.message = '上传中'
    try {
      const data = await uploadImage(item.file)
      item.remoteUrl = data.url
      item.status = 'done'
      item.message = ''
    } catch (e) {
      item.status = 'failed'
      item.message = '上传失败'
    }
  }
}

const onOversize = () => showToast('单张图片不能超过 10MB')

const onSubmit = async () => {
  if (!selected.value) {
    showToast('请选择要打卡的菜谱')
    return
  }
  const images = fileList.value.map((item) => item.remoteUrl).filter(Boolean)
  if (!content.value.trim() && !images.length) {
    showToast('写点心得或上传图片再打卡吧')
    return
  }
  submitting.value = true
  try {
    await createCheckIn({
      recipeId: selected.value.id,
      content: content.value.trim(),
      images
    })
    showToast('打卡成功')
    content.value = ''
    fileList.value = []
    selected.value = null
    await Promise.all([loadToday(), loadMine()])
  } finally {
    submitting.value = false
  }
}

const loadToday = async () => {
  try {
    const data = await fetchTodayCount()
    todayCount.value = data.count || 0
  } catch (e) {
    todayCount.value = 0
  }
}

const loadMine = async () => {
  try {
    const data = await fetchMyCheckIns(1, 20)
    mine.value = data.records || []
  } catch (e) {
    mine.value = []
  }
}

const onRemove = async (item) => {
  try {
    await showConfirmDialog({ title: '删除打卡', message: '确定删除这条打卡记录吗？' })
  } catch (e) {
    return
  }
  await deleteCheckIn(item.id)
  showToast('已删除')
  await Promise.all([loadToday(), loadMine()])
}

const preview = (index, images) => {
  showImagePreview({ images, startPosition: index })
}

const onBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.replace('/')
  }
}

onMounted(async () => {
  loadToday()
  loadMine()
  // 从菜谱详情页「我也来打卡」进来时，默认带上这道菜
  const recipeId = route.query.recipeId
  if (recipeId) {
    try {
      selected.value = await fetchRecipeDetail(recipeId)
    } catch (e) {
      selected.value = null
    }
  }
})
</script>

<style scoped>
.checkin-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 80px;
}

.today-card {
  margin: 12px;
  padding: 18px;
  border-radius: 12px;
  background: linear-gradient(135deg, #ff9a6c, #ff7a45);
  color: #fff;
  text-align: center;
}

.today-num {
  font-size: 32px;
  font-weight: 700;
}

.today-label {
  margin-top: 4px;
  font-size: 12px;
  opacity: 0.9;
}

.block {
  margin-bottom: 12px;
}

.block-title {
  padding: 12px 16px 8px;
  font-size: 13px;
  color: #646566;
}

.block-title.plain {
  padding-left: 16px;
}

.upload-box {
  padding: 4px 16px 16px;
}

.submit-box {
  margin: 4px 16px 20px;
}

.mine-list {
  padding: 0 12px;
}

.mine-card {
  margin-bottom: 12px;
  padding: 12px;
  border-radius: 10px;
  background: #fff;
}

.mine-head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mine-recipe {
  flex: 1;
  min-width: 0;
}

.mine-time {
  font-size: 12px;
  color: #969799;
}

.mine-text {
  margin-top: 8px;
  font-size: 14px;
  line-height: 1.6;
  color: #323233;
}

.mine-images {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.mine-image {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  overflow: hidden;
}

.picker-page {
  height: 100%;
  overflow-y: auto;
}
</style>
