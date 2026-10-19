<template>
  <div class="recipe-detail-page">
    <van-nav-bar title="菜谱详情" left-arrow fixed placeholder @click-left="onBack" />

    <van-loading v-if="loading" class="page-loading" vertical>加载中...</van-loading>

    <template v-else-if="detail">
      <div class="cover-box">
        <van-image v-if="detail.coverUrl" :src="detail.coverUrl" fit="cover" class="cover">
          <template #error>
            <div class="cover-fallback big">{{ detail.name?.slice(0, 1) }}</div>
          </template>
        </van-image>
        <div v-else class="cover-fallback big">{{ detail.name?.slice(0, 1) }}</div>
      </div>

      <div class="summary">
        <div class="name-row">
          <span class="name">{{ detail.name }}</span>
          <van-tag v-if="detail.isRecommend === 1" type="danger">招牌</van-tag>
        </div>
        <div v-if="detail.subtitle" class="subtitle">{{ detail.subtitle }}</div>

        <div class="price-row">
          <span class="price">¥{{ detail.price }}</span>
          <span v-if="detail.originalPrice" class="origin">¥{{ detail.originalPrice }}</span>
          <span class="sales">已售 {{ detail.sales ?? 0 }}</span>
        </div>

        <div class="meta">
          <span v-if="detail.categoryName">分类：{{ detail.categoryName }}</span>
          <span v-if="detail.cuisine">菜系：{{ detail.cuisine }}</span>
          <span v-if="detail.taste">口味：{{ detail.taste }}</span>
          <span v-if="detail.cookMethod">做法：{{ detail.cookMethod }}</span>
          <span v-if="detail.cookTime">耗时：{{ detail.cookTime }} 分钟</span>
          <span v-if="detail.servings">份量：{{ detail.servings }} 人份</span>
          <span v-if="detail.calories">热量：{{ detail.calories }} 千卡</span>
          <span>难度：{{ difficultyText }}</span>
        </div>

        <div v-if="detail.description" class="desc">{{ detail.description }}</div>
      </div>

      <van-tabs v-model:active="activeTab" sticky animated swipeable>
        <van-tab :title="`做法 ${detail.steps.length}`" name="steps">
          <div class="tab-body">
            <van-steps direction="vertical" :active="0" active-color="#ff7a45">
              <van-step v-for="step in detail.steps" :key="step.id">
                <div class="step-head">
                  <span class="step-no">第 {{ step.stepNo }} 步</span>
                  <span v-if="step.title" class="step-title">{{ step.title }}</span>
                  <van-tag v-if="step.duration" plain type="primary">{{ step.duration }} 分钟</van-tag>
                </div>
                <div class="step-content">{{ step.content }}</div>
                <van-image
                  v-if="step.imageUrl"
                  :src="step.imageUrl"
                  fit="cover"
                  class="step-image"
                />
                <div v-if="step.tip" class="step-tip">要点：{{ step.tip }}</div>
              </van-step>
            </van-steps>
            <van-empty v-if="!detail.steps.length" description="还没有录入做法步骤" />
          </div>
        </van-tab>

        <van-tab :title="`食材 ${detail.ingredients.length}`" name="ingredients">
          <div class="tab-body">
            <van-cell-group inset>
              <van-cell
                v-for="item in detail.ingredients"
                :key="item.id"
                :title="item.name"
                :value="`${item.amount || ''}${item.unit || ''}`"
                :label="item.note"
              >
                <template #icon>
                  <van-tag
                    class="cell-tag"
                    :type="item.isMain === 1 ? 'danger' : 'default'"
                    size="medium"
                  >
                    {{ item.isMain === 1 ? '主料' : '辅料' }}
                  </van-tag>
                </template>
              </van-cell>
            </van-cell-group>
            <van-empty v-if="!detail.ingredients.length" description="还没有录入食材" />
          </div>
        </van-tab>

        <van-tab :title="`调料 ${detail.seasonings.length}`" name="seasonings">
          <div class="tab-body">
            <van-cell-group inset>
              <van-cell
                v-for="item in detail.seasonings"
                :key="item.id"
                :title="item.name"
                :value="`${item.amount || ''}${item.unit || ''}`"
                :label="item.note"
              />
            </van-cell-group>
            <van-empty v-if="!detail.seasonings.length" description="还没有录入调料" />
          </div>
        </van-tab>

        <van-tab :title="`技巧 ${detail.tips.length}`" name="tips">
          <div class="tab-body">
            <van-collapse v-model="activeTip" accordion>
              <van-collapse-item
                v-for="(item, index) in detail.tips"
                :key="item.id"
                :name="index"
                :title="item.title"
              >
                {{ item.content }}
              </van-collapse-item>
            </van-collapse>
            <van-empty v-if="!detail.tips.length" description="还没有录入烹饪技巧" />
          </div>
        </van-tab>

        <van-tab :title="`评论 ${commentCount}`" name="comments">
          <div class="tab-body">
            <div v-if="comments.length" class="comment-list">
              <div v-for="item in comments" :key="item.id" class="comment-item">
                <div class="comment-head">
                  <div class="comment-avatar">{{ item.userNickname?.slice(0, 1) || 'U' }}</div>
                  <div class="comment-who">
                    <div class="comment-nick">{{ item.userNickname }}</div>
                    <div class="comment-time">{{ item.createTime }}</div>
                  </div>
                  <van-button
                    v-if="item.mine"
                    size="mini"
                    type="danger"
                    plain
                    @click="onDeleteComment(item.id)"
                  >
                    删除
                  </van-button>
                </div>
                <div v-if="item.content" class="comment-text">{{ item.content }}</div>
                <div v-if="item.images.length" class="comment-images">
                  <van-image
                    v-for="(img, index) in item.images"
                    :key="index"
                    :src="img"
                    fit="cover"
                    class="comment-image"
                    @click="previewComment(index, item.images)"
                  />
                </div>

                <div class="comment-replies">
                  <div class="reply-toggle">
                    <span class="reply-count" @click="toggleReplies(item)">
                      {{
                        expandedId === item.id
                          ? '收起回复'
                          : `查看回复（${item.replyCount || 0}）`
                      }}
                    </span>
                    <span class="reply-action" @click="openReply(item)">回复</span>
                  </div>

                  <div v-if="expandedId === item.id" class="reply-list">
                    <div
                      v-for="reply in replyMap[item.id]?.list || []"
                      :key="reply.id"
                      class="reply-item"
                    >
                      <span class="reply-nick">{{ reply.userNickname }}</span>
                      <span v-if="reply.replyToNickname" class="reply-to">
                        回复 {{ reply.replyToNickname }}
                      </span>
                      <span class="reply-text">：{{ reply.content }}</span>
                      <span class="reply-ops">
                        <span @click="openReply(item, reply)">回复</span>
                        <span
                          v-if="reply.mine"
                          class="danger"
                          @click="onDeleteReply(item.id, reply.id)"
                        >
                          删除
                        </span>
                      </span>
                    </div>
                    <div
                      v-if="replyMap[item.id]?.hasMore"
                      class="reply-more"
                      @click="loadMoreReplies(item)"
                    >
                      加载更多回复
                    </div>
                    <div v-if="!(replyMap[item.id]?.list || []).length" class="reply-empty">
                      还没有回复，来说两句吧
                    </div>
                  </div>
                </div>
              </div>

              <!-- 下滑到底自动加载下一页 -->
              <van-list
                v-model:loading="commentLoading"
                :finished="commentFinished"
                :immediate-check="false"
                finished-text="没有更多评论了"
                loading-text="加载中..."
                @load="loadComments"
              />
            </div>

            <van-empty v-else-if="commentLoaded" description="还没有人打卡，来做第一个吧" />

            <div class="checkin-entry">
              <van-button block type="primary" plain @click="goCheckIn">我也来打卡</van-button>
            </div>
          </div>
        </van-tab>
      </van-tabs>

      <van-action-bar>
        <van-action-bar-icon
          icon="cart-o"
          text="购物车"
          :badge="cartCount > 0 ? String(cartCount) : ''"
          to="/cart"
        />
        <van-action-bar-button type="warning" text="加入购物车" @click="onAddCart" />
        <van-action-bar-button type="danger" text="立即购买" @click="onBuyNow" />
      </van-action-bar>

      <ReplyInput
        v-model:show="replyVisible"
        :title="replyTitle"
        :placeholder="replyPlaceholder"
        :loading="replySending"
        @submit="submitReply"
      />
    </template>

    <van-empty v-else description="菜谱不存在或已下架" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showConfirmDialog, showImagePreview, showToast } from 'vant'
import { fetchRecipeDetail } from '@/api/recipe'
import { addCart, fetchCartCount } from '@/api/cart'
import { isLogin } from '@/api/auth'
import {
  createReply,
  deleteCheckIn,
  deleteReply,
  fetchCheckInCount,
  fetchCheckInPage,
  fetchReplies
} from '@/api/checkin'
import ReplyInput from '@/components/ReplyInput.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const detail = ref(null)
const activeTab = ref('steps')
const activeTip = ref(0)

const difficultyMap = { 1: '简单', 2: '中等', 3: '困难' }
const difficultyText = computed(() => difficultyMap[detail.value?.difficulty] || '简单')

const cartCount = ref(0)

const loadCartCount = async () => {
  // 未登录时购物车接口会返回 401，这里直接跳过，避免被当成登录失效跳走
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

const onAddCart = async () => {
  if (!isLogin()) {
    showToast('请先登录')
    router.push({ path: '/login', query: { redirect: `/recipe/${route.params.id}` } })
    return
  }
  await addCart({ recipeId: Number(route.params.id), quantity: 1 })
  showToast('已加入购物车')
  await loadCartCount()
}

const onBuyNow = () => {
  router.push({ path: '/order-confirm', query: { buy: route.params.id, qty: 1 } })
}

const onBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.replace('/')
  }
}

// ===== 评论（打卡） =====
const comments = ref([])
const commentCount = ref(0)
const commentPage = ref(0)
const commentLoading = ref(false)
const commentFinished = ref(false)
const commentLoaded = ref(false)

const loadComments = async () => {
  if (commentLoading.value || commentFinished.value) {
    return
  }
  commentLoading.value = true
  try {
    const page = await fetchCheckInPage(route.params.id, commentPage.value + 1, 10)
    comments.value = comments.value.concat(page.records || [])
    commentPage.value += 1
    commentFinished.value = comments.value.length >= (page.total || 0)
  } catch (e) {
    commentFinished.value = true
  } finally {
    commentLoading.value = false
    commentLoaded.value = true
  }
}

const loadCommentCount = async () => {
  try {
    const data = await fetchCheckInCount(route.params.id)
    commentCount.value = data.count || 0
  } catch (e) {
    commentCount.value = 0
  }
}

// 切到「评论」标签时才拉取，避免进详情页就多打一次接口
watch(activeTab, (tab) => {
  if (tab === 'comments' && !commentLoaded.value) {
    loadComments()
  }
})

const previewComment = (index, images) => {
  showImagePreview({ images, startPosition: index })
}

const onDeleteComment = async (id) => {
  try {
    await showConfirmDialog({ title: '删除打卡', message: '确定删除这条打卡记录吗？' })
  } catch (e) {
    return
  }
  await deleteCheckIn(id)
  showToast('已删除')
  comments.value = comments.value.filter((item) => item.id !== id)
  commentCount.value = Math.max(0, commentCount.value - 1)
}

const goCheckIn = () => {
  if (!isLogin()) {
    showToast('请先登录')
    router.push({ path: '/login', query: { redirect: `/recipe/${route.params.id}` } })
    return
  }
  router.push({ path: '/check-in', query: { recipeId: route.params.id } })
}

// ===== 评论的回复 =====
const expandedId = ref(null)
const replyMap = ref({})
const replyVisible = ref(false)
const replySending = ref(false)
const replyTarget = ref(null)

const replyTitle = computed(() =>
  replyTarget.value ? `回复 ${replyTarget.value.nickname}` : '写回复'
)
const replyPlaceholder = computed(() =>
  replyTarget.value ? `回复 @${replyTarget.value.nickname}` : '说点什么…'
)

const fetchReplyPage = async (checkInId) => {
  const state = replyMap.value[checkInId] || { list: [], page: 0, hasMore: false }
  const next = state.page + 1
  const data = await fetchReplies(checkInId, next, 10)
  const list = state.list.concat(data.records || [])
  replyMap.value = {
    ...replyMap.value,
    [checkInId]: { list, page: next, hasMore: list.length < (data.total || 0) }
  }
}

const toggleReplies = async (item) => {
  if (expandedId.value === item.id) {
    expandedId.value = null
    return
  }
  expandedId.value = item.id
  if (!replyMap.value[item.id]) {
    await fetchReplyPage(item.id)
  }
}

const loadMoreReplies = async (item) => {
  await fetchReplyPage(item.id)
}

const openReply = (item, reply = null) => {
  if (!isLogin()) {
    showToast('请先登录')
    router.push({ path: '/login', query: { redirect: `/recipe/${route.params.id}` } })
    return
  }
  replyTarget.value = {
    checkInId: item.id,
    userId: reply ? reply.userId : item.userId,
    nickname: reply ? reply.userNickname : item.userNickname
  }
  replyVisible.value = true
}

/** 重置某条评论的回复缓存为第一页 */
const resetReplies = async (checkInId) => {
  replyMap.value = { ...replyMap.value, [checkInId]: { list: [], page: 0, hasMore: false } }
  await fetchReplyPage(checkInId)
}

const bumpReplyCount = (checkInId, delta) => {
  const item = comments.value.find((c) => c.id === checkInId)
  if (item) {
    item.replyCount = Math.max(0, (item.replyCount || 0) + delta)
  }
}

const submitReply = async (text) => {
  const target = replyTarget.value
  if (!target) {
    return
  }
  replySending.value = true
  try {
    await createReply(target.checkInId, { content: text, replyToUserId: target.userId })
    showToast('回复成功')
    replyVisible.value = false
    await resetReplies(target.checkInId)
    bumpReplyCount(target.checkInId, 1)
  } finally {
    replySending.value = false
  }
}

const onDeleteReply = async (checkInId, replyId) => {
  try {
    await showConfirmDialog({ title: '删除回复', message: '确定删除这条回复吗？' })
  } catch (e) {
    return
  }
  await deleteReply(replyId)
  showToast('已删除')
  await resetReplies(checkInId)
  bumpReplyCount(checkInId, -1)
}

onMounted(async () => {
  loading.value = true
  try {
    detail.value = await fetchRecipeDetail(route.params.id)
    await loadCartCount()
    loadCommentCount()
  } catch (e) {
    detail.value = null
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.recipe-detail-page {
  padding-bottom: 130px;
}

.page-loading {
  padding: 60px 0;
}

.comment-list {
  padding: 12px 12px 0;
}

.comment-item {
  padding: 12px;
  margin-bottom: 10px;
  border-radius: 10px;
  background: #fff;
}

.comment-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.comment-avatar {
  width: 34px;
  height: 34px;
  flex: none;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff9a6c, #ff7a45);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.comment-who {
  flex: 1;
  min-width: 0;
}

.comment-nick {
  font-size: 14px;
  font-weight: 600;
  color: #323233;
}

.comment-time {
  margin-top: 2px;
  font-size: 12px;
  color: #969799;
}

.comment-text {
  margin-top: 8px;
  font-size: 14px;
  line-height: 1.6;
  color: #323233;
  word-break: break-all;
}

.comment-images {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.comment-image {
  width: 84px;
  height: 84px;
  border-radius: 6px;
  overflow: hidden;
}

.checkin-entry {
  padding: 16px 12px 8px;
}

.comment-replies {
  margin-top: 8px;
}

.reply-toggle {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
}

.reply-count {
  color: #1989fa;
}

.reply-action {
  color: #ff7a45;
}

.reply-list {
  margin-top: 8px;
  padding: 8px 10px;
  border-radius: 6px;
  background: #f7f8fa;
}

.reply-item {
  font-size: 13px;
  line-height: 1.7;
  color: #323233;
  word-break: break-all;
}

.reply-nick {
  color: #1989fa;
}

.reply-to {
  margin-left: 2px;
  font-size: 12px;
  color: #969799;
}

.reply-ops {
  margin-left: 6px;
  font-size: 12px;
  color: #1989fa;
}

.reply-ops span {
  margin-right: 8px;
}

.reply-ops .danger {
  color: #ee0a24;
}

.reply-more {
  margin-top: 6px;
  font-size: 12px;
  color: #1989fa;
}

.reply-empty {
  font-size: 12px;
  color: #969799;
}

.cover-box {
  height: 220px;
}

.cover {
  width: 100%;
  height: 220px;
}

.cover-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ff9a6c, #ff7a45);
  color: #fff;
  font-weight: 600;
}

.cover-fallback.big {
  height: 220px;
  font-size: 64px;
}

.summary {
  padding: 14px 16px;
  background: #fff;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name {
  font-size: 20px;
  font-weight: 600;
}

.subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: #969799;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-top: 10px;
}

.price {
  font-size: 22px;
  font-weight: 600;
  color: #ee0a24;
}

.origin {
  font-size: 13px;
  color: #c8c9cc;
  text-decoration: line-through;
}

.sales {
  margin-left: auto;
  font-size: 12px;
  color: #969799;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
  margin-top: 10px;
  font-size: 12px;
  color: #7d7e80;
}

.desc {
  margin-top: 10px;
  font-size: 13px;
  line-height: 20px;
  color: #646566;
}

.tab-body {
  padding: 12px 0 24px;
  background: #f7f8fa;
}

.step-head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.step-no {
  font-size: 14px;
  font-weight: 600;
  color: #ff7a45;
}

.step-title {
  font-size: 14px;
  font-weight: 600;
}

.step-content {
  margin-top: 6px;
  font-size: 14px;
  line-height: 22px;
  color: #323233;
}

.step-image {
  margin-top: 8px;
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
}

.step-tip {
  margin-top: 6px;
  padding: 6px 10px;
  border-radius: 6px;
  background: #fff7e8;
  font-size: 12px;
  color: #b26a00;
}

.cell-tag {
  margin-right: 8px;
}
</style>
