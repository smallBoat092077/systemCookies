<template>
  <van-popup v-model:show="innerShow" position="bottom" round>
    <div class="reply-box">
      <div class="reply-title">{{ title || '写回复' }}</div>
      <van-field
        v-model="text"
        type="textarea"
        rows="3"
        maxlength="500"
        show-word-limit
        :placeholder="placeholder"
      />
      <div class="reply-actions">
        <van-button size="small" plain @click="close">取消</van-button>
        <van-button
          size="small"
          type="primary"
          :loading="loading"
          :disabled="!text.trim()"
          @click="onSubmit"
        >
          发送
        </van-button>
      </div>
    </div>
  </van-popup>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  show: { type: Boolean, default: false },
  title: { type: String, default: '' },
  placeholder: { type: String, default: '说点什么…' },
  loading: { type: Boolean, default: false }
})

const emit = defineEmits(['update:show', 'submit'])

const innerShow = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value)
})

const text = ref('')

watch(
  () => props.show,
  (visible) => {
    if (visible) {
      text.value = ''
    }
  }
)

const close = () => {
  innerShow.value = false
}

const onSubmit = () => {
  if (!text.value.trim()) {
    return
  }
  emit('submit', text.value.trim())
}
</script>

<style scoped>
.reply-box {
  padding: 16px 16px 20px;
}

.reply-title {
  margin-bottom: 10px;
  font-size: 15px;
  font-weight: 600;
  color: #323233;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 12px;
}
</style>
