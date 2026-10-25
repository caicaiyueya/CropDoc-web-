import { defineStore } from 'pinia'
import { ref } from 'vue'

const STORAGE_KEY = 'ynzz_diagnosis_history_v1'

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
function loadFromStorage() {
  try {
    const raw = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
    return Array.isArray(raw) ? raw : []
  } catch {
    return []
  }
}

function persist(list) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(list))
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
  /** @type {import('vue').Ref<Array<Record<string, any>>>} */
  const records = ref(loadFromStorage())

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
    persist(records.value)
    return record
  }

  /** 清空全部历史并同步 localStorage */
  function clearRecords() {
    records.value = []
    persist(records.value)
  }

  return {
    records,
    addRecord,
    getRecords,
    clearRecords
  }
})
