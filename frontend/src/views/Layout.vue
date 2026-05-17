<template>
  <div style="display: flex; height: 100vh;">
    <!-- 侧边栏 -->
    <div style="width: 220px; background: #304156; color: white; padding: 20px;">
      <h2 style="color: white;">📋 智能审理</h2>
      <nav style="margin-top: 20px;">
        <div v-for="item in menuItems" :key="item.path" 
             style="padding: 10px; cursor: pointer; margin: 5px 0; border-radius: 4px;"
             :style="{ background: activeMenu === item.path ? '#409eff' : 'transparent' }"
             @click="$router.push(item.path)">
          {{ item.icon }} {{ item.name }}
        </div>
      </nav>
    </div>
    
    <!-- 主体内容 -->
    <div style="flex: 1; display: flex; flex-direction: column;">
      <header style="background: white; padding: 0 20px; height: 60px; display: flex; align-items: center; border-bottom: 1px solid #e6e6e6;">
        <span style="font-weight: 600;">{{ currentTitle }}</span>
      </header>
      <main style="flex: 1; background: #f0f2f5; padding: 20px;">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '智能审理系统')

const menuItems = [
  { path: '/dashboard', name: '工作台', icon: '🏠' },
  { path: '/case', name: '案件管理', icon: '⚖️' },
  { path: '/document/parse', name: '文档解析', icon: '📄' },
  { path: '/incoming-doc', name: '来文登记', icon: '📨' },
  { path: '/report', name: '文书生成', icon: '📝' },
  { path: '/promotion', name: '以案促改', icon: '🔍' },
  { path: '/workflow', name: '工作流', icon: '✅' },
  { path: '/classification', name: '定密建议', icon: '🔒' },
  { path: '/punishment', name: '处分执行', icon: '⚖️' },
  { path: '/readingnote', name: '阅卷笔记', icon: '📒' },
  { path: '/repo/laws', name: '法规库', icon: '📚' },
  { path: '/repo/cases', name: '案例库', icon: '📚' },
  { path: '/documents', name: '多库管理', icon: '📚' },
  { path: '/upload', name: '文档上传', icon: '📤' },
  { path: '/categories', name: '目录管理', icon: '📁' },
  { path: '/system/user', name: '用户管理', icon: '👤' },
  { path: '/system/role', name: '角色管理', icon: '🔑' },
  { path: '/system/menu', name: '菜单管理', icon: '📋' },
  { path: '/system/log', name: '审计日志', icon: '📝' },
]
</script>
