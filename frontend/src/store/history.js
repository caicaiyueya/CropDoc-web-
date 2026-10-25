import { defineStore } from 'pinia'
import { ref } from 'vue'

const STORAGE_PREFIX = 'ynzz_diagnosis_history_v1'
/** 旧版全局 key，首次按用户绑定时迁移一次后删除 */
const LEGACY_STORAGE_KEY = 'ynzz_diagnosis_history_v1'

/**
 * 统一诊断记录结构：
 * {
 *   id: string,                              // 唯一 ID（默认时间戳）
 *   type: 'DISEASE_DETECT' | 'AI_CONSULT',   // 识别结果 / AI 问诊
 *   crop: string,                            // 作物名称
 *   diseaseName: string,                     // 病害名称（中文）
 *   confidence: number,                      // 置信度（0~1 或 百分比均可）
 *   imageUrl: string,                        // 相关图片
 *   createTime: string,                      // 'YYYY-MM-DD HH:mm:ss'
 *   summary: string                          // 简短摘要 / 诊断结论
 * }
 */
function storageKey(userId) {
  if (userId == null || userId === '') return null
  return `${STORAGE_PREFIX}_${userId}`
}

function readList(key) {
  if (!key) return []
  try {
    const raw = JSON.parse(localStorage.getItem(key) || '[]')
    return Array.isArray(raw) ? raw : []
  } catch {
    return []
  }
}

function persist(key, list) {
  if (!key) return
  localStorage.setItem(key, JSON.stringify(list))
}

function formatNow() {
  const d = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function todayPrefix() {
  return formatNow().slice(0, 10)
}

export const useHistoryStore = defineStore('history', () => {
  const boundUserId = ref(null)
  /** @type {import('vue').Ref<Array<Record<string, any>>>} */
  const records = ref([])

  function activeKey() {
    return storageKey(boundUserId.value)
  }

  /**
   * 绑定当前登录用户并加载其本地历史。
   * 若仅有旧版全局数据，则迁移到当前用户后删除全局 key。
   * @param {string|number|null|undefined} userId
   */
  function bindUser(userId) {
    const id = userId == null || userId === '' ? null : String(userId)
    if (id === boundUserId.value && id != null) {
      // 同用户重复绑定：仍从存储刷新，避免脏内存
      records.value = readList(activeKey())
      return
    }
    boundUserId.value = id
    if (!id) {
      records.value = []
      return
    }
    const key = storageKey(id)
    let list = readList(key)
    if (!list.length) {
      const legacy = readList(LEGACY_STORAGE_KEY)
      if (legacy.length) {
        list = legacy
        persist(key, list)
        localStorage.removeItem(LEGACY_STORAGE_KEY)
      }
    }
    records.value = list
  }

  /** 退出登录：仅清空内存，保留各用户 localStorage 分桶 */
  function resetOnLogout() {
    boundUserId.value = null
    records.value = []
    // 尚未迁移的旧全局数据不得留给下一个登录账号
    try {
      localStorage.removeItem(LEGACY_STORAGE_KEY)
    } catch {
      // ignore
    }
  }

  /** 读取全部诊断记录（响应式数组引用） */
  function getRecords() {
    return records.value
  }

  /**
   * 追加一条诊断记录到最前，并写入 localStorage。
   * @param {object} partial
   * @param {{ dedupeDiseaseDay?: boolean }} [opts] 同 type+diseaseName+当天去重（用于问诊自动写入）
   */
  function addRecord(partial, opts = {}) {
    if (!boundUserId.value) return null

    const diseaseName = String(partial?.diseaseName || '').trim()
    const type = partial?.type || 'DISEASE_DETECT'

    if (opts.dedupeDiseaseDay && diseaseName) {
      const day = todayPrefix()
      const exists = records.value.some(
        (r) =>
          r.type === type &&
          r.diseaseName === diseaseName &&
          String(r.createTime || '').startsWith(day)
      )
      if (exists) return null
    }

    const record = {
      id: partial?.id || String(Date.now()),
      type,
      crop: partial?.crop || '',
      diseaseName: diseaseName || '未知',
      confidence: Number(partial?.confidence) || 0,
      imageUrl: partial?.imageUrl || '',
      createTime: partial?.createTime || formatNow(),
      summary: partial?.summary || ''
    }

    records.value = [record, ...records.value]
    persist(activeKey(), records.value)
    return record
  }

  /**
   * 按 id 删除单条记录，并同步 localStorage。
   * @param {string|number} id
   * @returns {boolean} 是否删除成功
   */
  function removeRecord(id) {
    if (!boundUserId.value) return false
    const before = records.value.length
    records.value = records.value.filter((r) => String(r.id) !== String(id))
    if (records.value.length === before) return false
    persist(activeKey(), records.value)
    return true
  }

  /** 清空当前用户全部历史并同步 localStorage */
  function clearRecords() {
    records.value = []
    const key = activeKey()
    if (key) persist(key, records.value)
  }

  /**
   * 用后端病害识别记录（含 image_path）合并进本地列表：
   * - 仅保留当前用户本地的 AI 问诊 / 非 api 识别记录
   * - 按 api-{id} 去重写入识别记录
   * @param {object[]} apiList DetectionRecord[]
   * @param {Record<string|number, string>} [cropNameById]
   */
  function mergeDetectionApi(apiList, cropNameById = {}) {
    if (!boundUserId.value) return records.value

    const formatTime = (t) => {
      if (!t) return ''
      return String(t).replace('T', ' ').slice(0, 19)
    }
    const resolveImage = (path) => {
      if (!path) return ''
      const p = String(path)
      if (p.startsWith('http') || p.startsWith('/') || p.startsWith('data:')) return p
      return `/uploads/${p}`
    }

    const fromApi = (apiList || []).map((r) => ({
      id: `api-${r.id}`,
      type: 'DISEASE_DETECT',
      crop: cropNameById[r.cropId] || cropNameById[String(r.cropId)] || '',
      diseaseName: r.result || '未知',
      confidence: Number(r.confidence) || 0,
      imageUrl: resolveImage(r.imagePath),
      createTime: formatTime(r.createTime),
      summary: r.description || '',
      _fromApi: true
    }))

    const localKeep = records.value.filter(
      (r) => r.type === 'AI_CONSULT' || (r.type === 'DISEASE_DETECT' && !String(r.id).startsWith('api-'))
    )
    const apiIds = new Set(fromApi.map((r) => r.id))
    const merged = [
      ...fromApi,
      ...localKeep.filter((r) => !apiIds.has(String(r.id)))
    ].sort((a, b) => String(b.createTime || '').localeCompare(String(a.createTime || '')))

    records.value = merged
    persist(activeKey(), records.value)
    return merged
  }

  return {
    records,
    boundUserId,
    bindUser,
    resetOnLogout,
    addRecord,
    getRecords,
    removeRecord,
    clearRecords,
    mergeDetectionApi
  }
})
