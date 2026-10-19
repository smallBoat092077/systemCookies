import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 顾客端：移动端 H5，开发端口 5173，接口走 /api 代理到后端 8088
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    host: '0.0.0.0',
    port: 5173,
    allowedHosts:['grinning-implosion-scone.ngrok-free.dev'],
    proxy: {
      '/api': {
        target: 'http://localhost:8088',
        changeOrigin: true
      },
      // 打卡图片由后端本地目录提供，同样走代理
      '/uploads': {
        target: 'http://localhost:8088',
        changeOrigin: true
      }
    }
  }
})
