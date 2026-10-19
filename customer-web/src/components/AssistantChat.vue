<template>
  <van-popup
    v-model:show="innerShow"
    position="bottom"
    :style="{ height: '72%' }"
    round
    teleport="body"
  >
    <div class="chat-page">
      <div class="chat-head">
        <div class="title">
          <span class="robot">🤖</span>
          <span>点菜小助理</span>
          <van-tag v-if="mode" :type="mode === 'ai' ? 'success' : 'primary'" size="mini">
            {{ mode === 'ai' ? 'AI 智能' : '本地助手' }}
          </van-tag>
        </div>
        <van-button size="mini" plain @click="onReset">重新开始</van-button>
      </div>

      <div ref="listRef" class="chat-body">
        <div class="tip">
          我可以帮你推荐菜品、加入购物车并直接下单。试试说「来一份红烧肉」或「看看购物车」。
        </div>
        <div v-for="(msg, index) in messages" :key="index" :class="['msg', msg.role]">
          <span v-if="msg.role === 'bot'" class="face">🤖</span>
          <div class="bubble">{{ msg.text }}</div>
        </div>
        <div v-if="loading" class="msg bot">
          <span class="face">🤖</span>
          <div class="bubble typing">正在想想…</div>
        </div>
      </div>

      <div class="chat-foot">
        <div v-if="suggestions.length" class="suggestions">
          <span
            v-for="item in suggestions"
            :key="item"
            class="chip"
            @click="useSuggestion(item)"
          >
            {{ item }}
          </span>
        </div>
        <div class="input-row">
          <van-field v-model="text" placeholder="想吃点什么？" @keyup.enter="onSend" />
          <van-button type="primary" size="small" :loading="loading" @click="onSend">
            发送
          </van-button>
        </div>
      </div>
    </div>
  </van-popup>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { clearChatSession, sendChat } from '@/api/assistant'
import { isLogin } from '@/api/auth'

const props = defineProps({
  show: { type: Boolean, default: false }
})
const emit = defineEmits(['update:show'])

const router = useRouter()

const innerShow = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value)
})

const SESSION_KEY = 'assistant:sessionId'
const WELCOME = '你好呀～想吃点什么？我可以推荐招牌菜，也可以直接帮你下单。'

const messages = ref([])
const text = ref('')
const loading = ref(false)
const mode = ref('')
const suggestions = ref(['今天有什么推荐', '来一份红烧肉'])
const listRef = ref(null)

const getSessionId = () => {
  let sessionId = localStorage.getItem(SESSION_KEY)
  if (!sessionId) {
    sessionId = 'web-' + Date.now() + '-' + Math.random().toString(36).slice(2, 8)
    localStorage.setItem(SESSION_KEY, sessionId)
  }
  return sessionId
}

const scrollToBottom = async () => {
  await nextTick()
  if (listRef.value) {
    listRef.value.scrollTop = listRef.value.scrollHeight
  }
}

const onSend = async () => {
  const content = text.value.trim()
  if (!content || loading.value) {
    return
  }
  if (!isLogin()) {
    showToast('登录后小助理才能帮你下单哦')
    innerShow.value = false
    router.push({ path: '/login', query: { redirect: '/' } })
    return
  }

  messages.value.push({ role: 'user', text: content })
  text.value = ''
  suggestions.value = []
  loading.value = true
  await scrollToBottom()

  try {
    const data = await sendChat({ sessionId: getSessionId(), message: content })
    mode.value = data.mode
    messages.value.push({ role: 'bot', text: data.reply })
    suggestions.value = data.suggestions || []

    const actions = data.actions || []
    if (actions.some((item) => item.type === 'addCart' || item.type === 'submitOrder')) {
      // 让底部导航的购物车角标立即刷新
      window.dispatchEvent(new Event('cart:changed'))
    }
    const order = actions.find((item) => item.type === 'submitOrder')
    if (order) {
      showToast('下单成功，可在「订单」查看')
    }
  } catch (e) {
    console.error('[assistant] 对话失败', e)
    messages.value.push({ role: 'bot', text: '网络不太好，稍后再试一次～' })
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

const useSuggestion = (item) => {
  text.value = item
  onSend()
}

const onReset = async () => {
  try {
    await clearChatSession(getSessionId())
  } catch (e) {
    // 会话可能已过期，本地同样重置
  }
  localStorage.removeItem(SESSION_KEY)
  messages.value = [{ role: 'bot', text: WELCOME }]
  suggestions.value = ['今天有什么推荐', '来一份红烧肉']
}

watch(innerShow, (visible) => {
  if (visible && !messages.value.length) {
    messages.value = [{ role: 'bot', text: WELCOME }]
  }
  if (visible) {
    scrollToBottom()
  }
})
</script>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #f2f3f5;
}

.title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  font-weight: 600;
}

.robot {
  font-size: 20px;
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px 14px;
  background: #f7f8fa;
}

.tip {
  padding: 8px 10px;
  margin-bottom: 10px;
  border-radius: 8px;
  background: #fff7e8;
  font-size: 12px;
  line-height: 1.6;
  color: #b26a00;
}

.msg {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  margin-bottom: 12px;
}

.msg.user {
  justify-content: flex-end;
}

.face {
  font-size: 18px;
}

.bubble {
  max-width: 76%;
  padding: 9px 12px;
  border-radius: 10px;
  background: #fff;
  font-size: 14px;
  line-height: 1.6;
  color: #323233;
  word-break: break-all;
}

.msg.user .bubble {
  background: linear-gradient(135deg, #ff9a6c, #ff7a45);
  color: #fff;
}

.typing {
  color: #969799;
}

.chat-foot {
  padding: 8px 12px 12px;
  border-top: 1px solid #f2f3f5;
  background: #fff;
}

.suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}

.chip {
  padding: 4px 10px;
  border: 1px solid #ffd0b8;
  border-radius: 14px;
  background: #fff7f2;
  font-size: 12px;
  color: #ff7a45;
}

.input-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.input-row :deep(.van-field) {
  flex: 1;
  padding: 4px 8px;
  border: 1px solid #ebedf0;
  border-radius: 18px;
}
</style>
