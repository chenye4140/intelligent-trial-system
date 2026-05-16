import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '工作台' },
      },
      {
        path: 'case',
        name: 'CaseManagement',
        component: () => import('@/views/case/CaseManagement.vue'),
        meta: { title: '案件管理' },
      },
      // 文档解析
      {
        path: 'document/parse',
        name: 'DocumentParse',
        component: () => import('@/views/document/Parse.vue'),
        meta: { title: '文档解析' },
      },
      // 文书生成
      {
        path: 'report',
        name: 'Report',
        component: () => import('@/views/report/Report.vue'),
        meta: { title: '文书生成' },
      },
      // 以案促改
      {
        path: 'promotion',
        name: 'Promotion',
        component: () => import('@/views/promotion/Promotion.vue'),
        meta: { title: '以案促改' },
      },
      // 工作流
      {
        path: 'workflow',
        name: 'Workflow',
        component: () => import('@/views/workflow/Workflow.vue'),
        meta: { title: '工作流' },
      },
      // 五级定密建议
      {
        path: 'classification',
        name: 'ClassificationSuggestion',
        component: () => import('@/views/classification/ClassificationSuggestion.vue'),
        meta: { title: '定密建议' },
      },
      // 法规库/案例库
      {
        path: 'repo/:type',
        name: 'RepositoryIndex',
        component: () => import('@/views/repository/Index.vue'),
        meta: { title: '资源库' },
      },
      {
        path: 'repo/:type/directory',
        name: 'RepositoryDirectory',
        component: () => import('@/views/repository/Directory.vue'),
        meta: { title: '目录管理' },
      },
      // 旧路由兼容
      {
        path: 'documents',
        name: 'Documents',
        component: () => import('@/views/Documents.vue'),
        meta: { title: '多库管理' },
      },
      {
        path: 'upload',
        name: 'Upload',
        component: () => import('@/views/DocumentUpload.vue'),
        meta: { title: '文档上传' },
      },
      {
        path: 'categories',
        name: 'Categories',
        component: () => import('@/views/Categories.vue'),
        meta: { title: '目录管理' },
      },
      // 处分执行
      {
        path: 'punishment',
        name: 'Punishment',
        component: () => import('@/views/punishment/Punishment.vue'),
        meta: { title: '处分执行' },
      },
      // 系统管理
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/User.vue'),
        meta: { title: '用户管理' },
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('@/views/system/Role.vue'),
        meta: { title: '角色管理' },
      },
      {
        path: 'system/menu',
        name: 'SystemMenu',
        component: () => import('@/views/system/Menu.vue'),
        meta: { title: '菜单管理' },
      },
      {
        path: 'system/log',
        name: 'SystemLog',
        component: () => import('@/views/system/Log.vue'),
        meta: { title: '审计日志' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
