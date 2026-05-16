import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserInfo, login as loginApi, logout as logoutApi } from '@/api/auth'
import { getMenuTree } from '@/api/menu'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const menus = ref([])
  const roles = ref([])

  // Login
  async function login(loginForm) {
    const res = await loginApi(loginForm)
    if (res.data) {
      token.value = res.data.token
      localStorage.setItem('token', res.data.token)
      if (loginForm.remember) {
        localStorage.setItem('rememberedUser', JSON.stringify({
          username: loginForm.username
        }))
      } else {
        localStorage.removeItem('rememberedUser')
      }
      return res
    }
  }

  // Get user info
  async function fetchUserInfo() {
    const res = await getUserInfo()
    if (res.data) {
      userInfo.value = res.data
      roles.value = res.data.roles || []
      return res
    }
  }

  // Get menu tree
  async function fetchMenus() {
    const res = await getMenuTree()
    if (res.data) {
      menus.value = res.data
      return res
    }
  }

  // Logout
  async function logout() {
    try {
      await logoutApi()
    } catch (e) {
      // ignore
    }
    token.value = ''
    userInfo.value = null
    menus.value = []
    roles.value = []
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    menus,
    roles,
    login,
    fetchUserInfo,
    fetchMenus,
    logout
  }
})
