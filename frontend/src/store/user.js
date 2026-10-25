import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { useHistoryStore } from '@/store/history'
import { clearSessionCaches, migrateLegacyAiChat } from '@/utils/userScopedCache'

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

  /** 同步诊断历史等到当前用户；退出则清空内存态 */
  function syncScopedStores(user) {
    const history = useHistoryStore()
    if (user?.id != null) {
      history.bindUser(user.id)
    } else {
      history.resetOnLogout()
    }
  }

  function setSession(newToken, user) {
    const prevId = userInfo.value?.id
    const nextId = user?.id
    const switched =
      prevId != null && nextId != null && String(prevId) !== String(nextId)
    const loggingOut = !newToken || !user

    // 退出或换号前：把旧版全局 AI 会话归到即将离开的用户，避免串给下一账号
    if ((loggingOut || switched) && prevId != null) {
      migrateLegacyAiChat(prevId)
    }

    token.value = newToken || ''
    userInfo.value = user || null
    if (newToken) localStorage.setItem(TOKEN_KEY, newToken)
    else localStorage.removeItem(TOKEN_KEY)
    if (user) localStorage.setItem(USER_KEY, JSON.stringify(user))
    else localStorage.removeItem(USER_KEY)

    if (loggingOut || switched) {
      clearSessionCaches()
    }
    syncScopedStores(user || null)
    if (user?.id != null) {
      migrateLegacyAiChat(user.id)
    }
  }

  function setUser(user) {
    userInfo.value = user
    if (user) localStorage.setItem(USER_KEY, JSON.stringify(user))
    if (user?.id != null) {
      useHistoryStore().bindUser(user.id)
    }
  }

  function logout() {
    setSession('', null)
  }

  // 页面刷新后：若已有登录态，立刻绑定对应用户历史，避免短暂串数据
  if (userInfo.value?.id != null) {
    syncScopedStores(userInfo.value)
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
