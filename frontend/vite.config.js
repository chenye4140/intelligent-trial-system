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
      // 认证 & 系统管理服务 (all /api/system/* and /api/auth/*)
      '/api/auth': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/system': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      // 案件管理服务（casemanage 模块，运行在 8085）
      '/api/case': {
        target: 'http://localhost:8085',
        changeOrigin: true,
      },
      // 文档解析 & 类案推送服务
      '/api/document': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      // 多库管理 & 目录服务
      '/api/repository': {
        target: 'http://localhost:8083',
        changeOrigin: true,
      },
      // 文书生成服务
      '/api/report': {
        target: 'http://localhost:8084',
        changeOrigin: true,
      },
      // 流程引擎服务
      '/api/workflow': {
        target: 'http://localhost:8086',
        changeOrigin: true,
      },
      // 以案促改分析服务
      '/api/promotion': {
        target: 'http://localhost:8088',
        changeOrigin: true,
      },
      // 处分执行服务
      '/api/punishment': {
        target: 'http://localhost:8089',
        changeOrigin: true,
      },
      // 阅卷笔记服务
      '/api/reading-note': {
        target: 'http://localhost:8090',
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
  },
})
