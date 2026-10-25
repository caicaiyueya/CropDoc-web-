/** 最近一次识别结果（sessionStorage），需带 userId 校验 */
export const LAST_DETECT_KEY = 'ynzz_last_detect'

/** AI 问诊会话 localStorage 前缀 */
export const AI_CHAT_PREFIX = 'ynzz_ai_chat_sessions_v1'
/** 旧版未按用户隔离的会话 key */
export const LEGACY_AI_CHAT_KEY = 'ynzz_ai_chat_sessions_v1'

export function aiChatStorageKey(userId) {
  if (userId == null || userId === '') return null
  return `${AI_CHAT_PREFIX}_${userId}`
}

/**
 * 将旧版全局 AI 会话迁移到指定用户（仅当该用户分桶为空时）。
 * @param {string|number|null|undefined} userId
 */
export function migrateLegacyAiChat(userId) {
  const key = aiChatStorageKey(userId)
  if (!key) return
  try {
    if (localStorage.getItem(key)) {
      localStorage.removeItem(LEGACY_AI_CHAT_KEY)
      return
    }
    const legacy = localStorage.getItem(LEGACY_AI_CHAT_KEY)
    if (!legacy) return
    localStorage.setItem(key, legacy)
    localStorage.removeItem(LEGACY_AI_CHAT_KEY)
  } catch {
    // ignore
  }
}

/** 退出 / 切换账号时清理跨用户易泄漏的 session 缓存 */
export function clearSessionCaches() {
  try {
    sessionStorage.removeItem(LAST_DETECT_KEY)
  } catch {
    // ignore
  }
  try {
    // 未迁移的旧全局会话不得留给下一账号
    localStorage.removeItem(LEGACY_AI_CHAT_KEY)
  } catch {
    // ignore
  }
}
