<template>
  <div class="login-page">
    <van-nav-bar title="登录 / 注册" left-arrow fixed placeholder @click-left="onBack" />

    <div class="brand">
      <div class="brand-icon">🍲</div>
      <div class="brand-text">食谱点菜</div>
      <div class="brand-sub">会做菜，也要会点菜</div>
    </div>

    <van-tabs v-model:active="active">
      <van-tab title="登录">
        <van-form @submit="onLogin">
          <van-cell-group inset>
            <van-field
              v-model="loginForm.username"
              label="账号"
              placeholder="请输入账号"
              :rules="[{ required: true, message: '请输入账号' }]"
            />
            <van-field
              v-model="loginForm.password"
              type="password"
              label="密码"
              placeholder="请输入密码"
              :rules="[{ required: true, message: '请输入密码' }]"
            />
          </van-cell-group>

<!--          <div class="demo-tip">演示账号：user1 / 123456</div>-->

          <div class="btn-box">
            <van-button round block type="primary" native-type="submit" :loading="submitting">
              登录
            </van-button>
          </div>
        </van-form>
      </van-tab>

      <van-tab title="注册">
        <van-form @submit="onRegister">
          <van-cell-group inset>
            <van-field
              v-model="regForm.username"
              label="账号"
              placeholder="4-20 位字母、数字或下划线"
              :rules="[
                { required: true, message: '请输入账号' },
                { pattern: /^[A-Za-z0-9_]{4,20}$/, message: '账号需为 4-20 位字母数字下划线' }
              ]"
            />
            <van-field
              v-model="regForm.password"
              type="password"
              label="密码"
              placeholder="6-20 位"
              :rules="[
                { required: true, message: '请输入密码' },
                { pattern: /^.{6,20}$/, message: '密码需为 6-20 位' }
              ]"
            />
            <van-field v-model="regForm.nickname" label="昵称" placeholder="选填，默认用账号" />
            <van-field
              v-model="regForm.phone"
              label="手机号"
              placeholder="选填"
              :rules="[{ pattern: /^(1[3-9]\d{9})?$/, message: '手机号格式不正确' }]"
            />
          </van-cell-group>

          <div class="btn-box">
            <van-button round block type="success" native-type="submit" :loading="submitting">
              注册并登录
            </van-button>
          </div>
        </van-form>
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { login, register, saveAuth } from '@/api/auth'

const route = useRoute()
const router = useRouter()

const active = ref(0)
const submitting = ref(false)

const loginForm = reactive({ username: '', password: '' })
const regForm = reactive({ username: '', password: '', nickname: '', phone: '' })

const redirect = computed(() => (typeof route.query.redirect === 'string' ? route.query.redirect : '/'))

const onLogin = async () => {
  submitting.value = true
  try {
    const data = await login({
      username: loginForm.username.trim(),
      password: loginForm.password
    })
    if (!data?.token) {
      showToast('登录状态保存失败，请重试')
      return
    }
    saveAuth(data)
    showToast(`欢迎回来，${data.user?.nickname || '你'}`)
    router.replace(redirect.value)
  } finally {
    submitting.value = false
  }
}

const onRegister = async () => {
  submitting.value = true
  try {
    const data = await register({
      username: regForm.username.trim(),
      password: regForm.password,
      nickname: regForm.nickname.trim(),
      phone: regForm.phone.trim()
    })
    if (!data?.token) {
      showToast('登录状态保存失败，请重试')
      return
    }
    saveAuth(data)
    showToast('注册成功')
    router.replace(redirect.value)
  } finally {
    submitting.value = false
  }
}

const onBack = () => router.replace('/')
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 24px;
}

.brand {
  padding: 30px 0 20px;
  text-align: center;
}

.brand-icon {
  font-size: 48px;
}

.brand-text {
  margin-top: 6px;
  font-size: 20px;
  font-weight: 600;
  color: #ff7a45;
}

.brand-sub {
  margin-top: 4px;
  font-size: 12px;
  color: #969799;
}

.demo-tip {
  margin: 10px 16px;
  padding: 8px 12px;
  border-radius: 6px;
  background: #fff7e8;
  font-size: 12px;
  color: #b26a00;
}

.btn-box {
  margin: 20px 16px 0;
}
</style>
