<template>
  <div class="login-wrap">
    <el-card class="login-card" shadow="always">
      <div class="brand">
        <span class="brand-icon">🍲</span>
        <span class="brand-text">食谱点菜 · 管理端</span>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="onSubmit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="管理员账号" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="submitting" @click="onSubmit">
          登录
        </el-button>
      </el-form>

      <div class="tip">演示账号：admin / admin123</div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login, saveAuth } from '@/api/auth'

const route = useRoute()
const router = useRouter()

const formRef = ref()
const submitting = ref(false)

const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const onSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  submitting.value = true
  try {
    const data = await login({ username: form.username.trim(), password: form.password })
    if (data.user.role !== 1) {
      ElMessage.error('该账号不是管理员，无法进入管理端')
      return
    }
    saveAuth(data)
    ElMessage.success(`欢迎回来，${data.user.nickname}`)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/recipes'
    router.replace(redirect)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.login-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: linear-gradient(135deg, #1f2d3d, #001529);
}

.login-card {
  width: 380px;
}

.brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.brand-icon {
  font-size: 24px;
}

.submit {
  width: 100%;
}

.tip {
  margin-top: 14px;
  text-align: center;
  font-size: 12px;
  color: #909399;
}
</style>
