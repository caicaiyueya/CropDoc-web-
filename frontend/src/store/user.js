import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

const TOKEN_KEY = 'ynzz_token'
const USER_KEY = 'ynzz_user'

/**
 * 用户会话 Store。
 * 对应 UML：会话中的「当前用户」对象；提供 hasRole。
 */
export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const userInfo = ref(JSON.parse(localStorage.getItem(USER_KEY) || 'null'))

  const role = computed(() => userInfo.value?.role || '')
  const isLogin = computed(() => !!token.value)
  const isExpert = computed(() => role.value === 'EXPERT' || role.value === 'ADMIN')
  const isAdmin = computed(() => role.value === 'ADMIN')
  const isFarmer = computed(() => role.value === 'FARMER')

  function hasRole(...roles) {
    if (!role.value) return false
    return roles.includes(role.value)
  }

  function setSession(newToken, user) {
    token.value = newToken || ''
    userInfo.value = user || null
    if (newToken) localStorage.setItem(TOKEN_KEY, newToken)
    else localStorage.removeItem(TOKEN_KEY)
    if (user) localStorage.setItem(USER_KEY, JSON.stringify(user))
    else localStorage.removeItem(USER_KEY)
  }

  function setUser(user) {
    userInfo.value = user
    if (user) localStorage.setItem(USER_KEY, JSON.stringify(user))
  }

  function logout() {
    setSession('', null)
  }

  return {
    token,
    userInfo,
    role,
    isLogin,
    isExpert,
    isAdmin,
    isFarmer,
    hasRole,
    setSession,
    setUser,
    logout
  }
})
