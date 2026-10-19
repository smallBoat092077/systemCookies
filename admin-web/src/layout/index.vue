<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="admin-aside">
      <div class="logo">
        <span class="logo-icon">🍲</span>
        <span>食谱点菜</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#001529"
        text-color="#bfcbd9"
        active-text-color="#ffd04b"
      >
        <el-menu-item index="/recipes">
          <el-icon><Dish /></el-icon>
          <span>菜谱管理</span>
        </el-menu-item>
        <el-menu-item index="/orders">
          <el-icon><Tickets /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/categories">
          <el-icon><Menu /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header height="56px" class="admin-header">
        <div class="header-title">{{ pageTitle }}</div>
        <div class="header-right">
          <span class="backend">后端服务：localhost:8088</span>
          <el-divider direction="vertical" />
          <el-icon><User /></el-icon>
          <span class="user-name">{{ user?.nickname || '未登录' }}</span>
          <el-tag size="small" type="danger">{{ user?.roleText || '-' }}</el-tag>
          <el-button link type="primary" @click="onLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Dish, Menu, Tickets, User } from '@element-plus/icons-vue'
import { getUser, logout, clearAuth } from '@/api/auth'

const route = useRoute()
const router = useRouter()

const user = ref(getUser())

const activeMenu = computed(() => route.meta?.activeMenu || route.path)
const pageTitle = computed(() => route.meta?.title || '')

const onLogout = async () => {
  try {
    await ElMessageBox.confirm('确定退出当前账号吗？', '退出登录', {
      type: 'warning',
      confirmButtonText: '退出',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }
  try {
    await logout()
  } catch (e) {
    // 令牌可能已失效，本地同样清理
  }
  clearAuth()
  ElMessage.success('已退出登录')
  router.replace('/login')
}
</script>

<style scoped>
.admin-layout {
  height: 100%;
}

.admin-aside {
  background: #001529;
  overflow: hidden;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 56px;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 2px;
}

.logo-icon {
  font-size: 22px;
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #606266;
}

.backend {
  font-size: 12px;
  color: #909399;
}

.user-name {
  font-weight: 600;
}

.admin-main {
  overflow: auto;
}

:deep(.el-menu) {
  border-right: none;
}
</style>
