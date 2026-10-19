<template>
  <div class="mine-page">
    <van-nav-bar title="我的" />

    <template v-if="user">
      <div class="user-card">
        <div class="avatar">{{ user.nickname?.slice(0, 1) || 'U' }}</div>
        <div class="user-info">
          <div class="nickname">
            {{ user.nickname }}
            <van-tag :type="user.role === 1 ? 'danger' : 'primary'" size="medium">
              {{ user.roleText }}
            </van-tag>
          </div>
          <div class="account">账号：{{ user.username }}</div>
          <div v-if="user.phone" class="account">手机：{{ user.phone }}</div>
        </div>
      </div>

      <van-tabs v-model:active="activeTab" animated swipeable>
        <van-tab :title="`我的评论 ${myCheckIns.length}`" name="mine">
          <div class="tab-body">
            <div v-if="myCheckIns.length" class="list">
              <div
                v-for="item in myCheckIns"
                :key="item.id"
                class="card"
                @click="goRecipe(item)"
              >
                <div class="card-head">
                  <van-tag type="primary" size="medium">{{ item.recipeName }}</van-tag>
                  <span class="time">{{ item.createTime }}</span>
                  <van-button size="mini" type="danger" plain @click.stop="onRemoveCheckIn(item)">
                    删除
                  </van-button>
                </div>
                <div v-if="item.content" class="text">{{ item.content }}</div>
                <div v-if="item.images.length" class="images">
                  <van-image
                    v-for="(img, index) in item.images"
                    :key="index"
                    :src="img"
                    fit="cover"
                    class="thumb"
                    @click.stop="preview(index, item.images)"
                  />
                </div>
                <div class="reply-tip">收到 {{ item.replyCount || 0 }} 条回复 · 查看菜谱</div>
              </div>
            </div>
            <van-empty v-else-if="loaded" description="还没有发布过打卡评论" />
          </div>
        </van-tab>

        <van-tab :title="`收到的回复 ${inbox.length}`" name="inbox">
          <div class="tab-body">
            <div v-if="inbox.length" class="list">
              <div v-for="item in inbox" :key="item.id" class="card">
                <div class="reply-head">
                  <div class="reply-avatar">{{ item.userNickname?.slice(0, 1) || 'U' }}</div>
                  <div class="who">
                    <div class="nick">
                      {{ item.userNickname }}
                      <span class="tip">回复了你</span>
                    </div>
                    <div class="time">{{ item.createTime }}</div>
                  </div>
                  <van-button size="mini" type="primary" plain @click="openReply(item)">
                    回复
                  </van-button>
                </div>
                <div class="text">{{ item.content }}</div>
                <div v-if="item.checkInContent" class="quote">
                  我的打卡：{{ item.checkInContent }}
                  <span v-if="item.checkInRecipeName">（{{ item.checkInRecipeName }}）</span>
                </div>
              </div>
            </div>
            <van-empty v-else-if="loaded" description="还没有收到回复" />
          </div>
        </van-tab>
      </van-tabs>

      <div class="btn-box">
        <van-button round block type="danger" plain @click="onLogout">退出登录</van-button>
      </div>
    </template>

    <div v-else class="guest">
      <van-empty description="还没有登录，登录后才能查看我的评论与回复">
        <van-button round type="primary" :to="loginPath">去登录 / 注册</van-button>
      </van-empty>
    </div>

    <ReplyInput
      v-model:show="replyVisible"
      :title="replyTitle"
      :placeholder="replyPlaceholder"
      :loading="replySending"
      @submit="submitReply"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showImagePreview, showToast } from 'vant'
import { authState, clearAuth, isLogin, logout, refreshUser } from '@/api/auth'
import { createReply, deleteCheckIn, fetchMyCheckIns, fetchReplyInbox } from '@/api/checkin'
import ReplyInput from '@/components/ReplyInput.vue'

const router = useRouter()

// 直接读全局登录态：登录成功后这里会立刻更新，不依赖组件是否重新挂载
const user = computed(() => authState.user)
const loginPath = { path: '/login', query: { redirect: '/mine' } }

const activeTab = ref('mine')
const myCheckIns = ref([])
const inbox = ref([])
const loaded = ref(false)

const loadMine = async () => {
  try {
    const data = await fetchMyCheckIns(1, 20)
    myCheckIns.value = data.records || []
  } catch (e) {
    myCheckIns.value = []
  }
}

const loadInbox = async () => {
  try {
    const data = await fetchReplyInbox(1, 20)
    inbox.value = data.records || []
  } catch (e) {
    inbox.value = []
  }
}

const loadAll = async () => {
  await Promise.all([loadMine(), loadInbox()])
  loaded.value = true
}

onMounted(async () => {
  if (!isLogin()) {
    loaded.value = true
    return
  }
  await refreshUser()
  await loadAll()
})

// 登录态变化（在本页登录、退出）时同步刷新列表
watch(
  () => authState.token,
  async (token) => {
    if (!token) {
      myCheckIns.value = []
      inbox.value = []
      loaded.value = true
      return
    }
    await refreshUser()
    await loadAll()
  }
)

const goRecipe = (item) => {
  router.push(`/recipe/${item.recipeId}`)
}

const preview = (index, images) => {
  showImagePreview({ images, startPosition: index })
}

const onRemoveCheckIn = async (item) => {
  try {
    await showConfirmDialog({ title: '删除打卡', message: '确定删除这条打卡记录吗？' })
  } catch (e) {
    return
  }
  await deleteCheckIn(item.id)
  showToast('已删除')
  await loadMine()
}

// ===== 回复 =====
const replyVisible = ref(false)
const replySending = ref(false)
const replyTarget = ref(null)

const replyTitle = computed(() =>
  replyTarget.value ? `回复 ${replyTarget.value.userNickname}` : '写回复'
)
const replyPlaceholder = computed(() =>
  replyTarget.value ? `回复 @${replyTarget.value.userNickname}` : '说点什么…'
)

const openReply = (item) => {
  replyTarget.value = item
  replyVisible.value = true
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
    await loadInbox()
  } finally {
    replySending.value = false
  }
}

const onLogout = async () => {
  try {
    await showConfirmDialog({ title: '退出登录', message: '确定要退出当前账号吗？' })
  } catch (e) {
    return
  }
  try {
    await logout()
  } catch (e) {
    // 令牌可能已过期，本地同样清理
  }
  clearAuth()
  showToast('已退出登录')
  router.replace('/')
}
</script>

<style scoped>
.mine-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 80px;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px 16px;
  background: linear-gradient(135deg, #ff9a6c, #ff7a45);
}

.avatar {
  width: 56px;
  height: 56px;
  flex: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  color: #ff7a45;
  font-size: 24px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-info {
  color: #fff;
}

.nickname {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 18px;
  font-weight: 600;
}

.account {
  margin-top: 4px;
  font-size: 12px;
  opacity: 0.9;
}

.tab-body {
  padding: 12px 12px 0;
}

.list {
  padding-bottom: 8px;
}

.card {
  padding: 12px;
  margin-bottom: 10px;
  border-radius: 10px;
  background: #fff;
}

.card-head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.time {
  flex: 1;
  font-size: 12px;
  color: #969799;
}

.text {
  margin-top: 8px;
  font-size: 14px;
  line-height: 1.6;
  color: #323233;
  word-break: break-all;
}

.images {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.thumb {
  width: 76px;
  height: 76px;
  border-radius: 6px;
  overflow: hidden;
}

.reply-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #ff7a45;
}

.reply-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.reply-avatar {
  width: 32px;
  height: 32px;
  flex: none;
  border-radius: 50%;
  background: linear-gradient(135deg, #ffb199, #ff7a45);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.who {
  flex: 1;
  min-width: 0;
}

.nick {
  font-size: 14px;
  font-weight: 600;
  color: #323233;
}

.tip {
  margin-left: 4px;
  font-size: 12px;
  font-weight: 400;
  color: #969799;
}

.quote {
  margin-top: 8px;
  padding: 8px 10px;
  border-radius: 6px;
  background: #f7f8fa;
  font-size: 12px;
  color: #646566;
  line-height: 1.5;
}

.guest {
  padding-top: 60px;
}

.btn-box {
  margin: 20px 16px 0;
}
</style>
