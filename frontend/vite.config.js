import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      // 文档解析 & 类案推送服务
      '/api/document': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        // 不重写路径 — 后端 Controller 使用 @RequestMapping("/api/document/...")
      },
      // 多库管理 & 目录服务
      '/api/repository': {
        target: 'http://localhost:8083',
        changeOrigin: true,
      },
      // 认证 & 系统管理服务
      '/api/auth': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/user': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/role': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/menu': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/dept': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/audit-log': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/classification': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      // 案件管理服务
      '/api/case': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
  },
})
