/**
 * 病害库：列表字段归一化 + 按名称匹配标准参考图（仅用于展示，不伪造业务数据）
 */

const COVER = '/images/hero-farm.jpg'
const POS = ['18% 40%', '72% 35%', '45% 70%', '30% 20%', '60% 55%', '25% 65%']

/** 标准病害参考图（按中文名匹配封面） */
export const DISEASE_IMAGES = [
  { name: '稻瘟病', image_url: '/images/diseases/rice-blast.png', coverPos: POS[0] },
  { name: '水稻纹枯病', image_url: '/images/diseases/rice-sheath-blight.png', coverPos: POS[1] },
  { name: '玉米普通锈病', image_url: '/images/diseases/corn-common-rust.png', coverPos: POS[2] },
  { name: '小麦赤霉病', image_url: '/images/diseases/wheat-fusarium.png', coverPos: POS[3] },
  { name: '番茄晚疫病', image_url: '/images/diseases/tomato-late-blight.png', coverPos: POS[4] },
  { name: '苹果黑星病', image_url: '/images/diseases/apple-scab.png', coverPos: POS[5] }
]

/** @deprecated 兼容旧引用，请改用 DISEASE_IMAGES */
export const MOCK_DISEASES = DISEASE_IMAGES

const DISEASE_ALIASES = {
  纹枯病专业防治: '水稻纹枯病',
  纹枯病: '水稻纹枯病',
  水稻纹枯: '水稻纹枯病'
}

function resolveImageByName(name) {
  const canonical = DISEASE_ALIASES[name] || name
  return (
    DISEASE_IMAGES.find((m) => m.name === canonical) ||
    DISEASE_IMAGES.find((m) => m.name === name) ||
    null
  )
}

/** 解析后端打包的 description：【发病原因】…【发病条件】…【描述】… */
export function parsePackedDiseaseFields(raw) {
  const text = String(raw || '')
  const causeMatch = text.match(/【发病原因】([\s\S]*?)(?:【发病条件】|【描述】|$)/)
  const condMatch = text.match(/【发病条件】([\s\S]*?)(?:【描述】|$)/)
  const descMatch = text.match(/【描述】\s*([\s\S]*)$/)
  if (causeMatch || condMatch || descMatch) {
    return {
      causes: (causeMatch?.[1] || '').trim(),
      conditions: (condMatch?.[1] || '').trim(),
      description: (descMatch?.[1] || '').trim() || text
    }
  }
  return { causes: '', conditions: '', description: text }
}

/**
 * 将后端 Disease 归一化为详情展示结构（内容仅来自接口字段）
 * @param {object} raw
 * @param {number} [index]
 */
export function normalizeDisease(raw, index = 0) {
  if (!raw) return null
  const rawName = raw.name || raw.title || '未知病害'
  const name = DISEASE_ALIASES[rawName] || rawName
  const img = resolveImageByName(rawName)
  const packed = parsePackedDiseaseFields(raw.description)
  const description = raw.causes || raw.conditions
    ? String(raw.description || packed.description || '').trim() || '暂无描述'
    : packed.description || String(raw.description || '').trim() || '暂无描述'
  const causes = String(raw.causes || raw.cause || packed.causes || '').trim() || '暂无资料'
  const conditions = String(raw.conditions || raw.condition || packed.conditions || '').trim() || '暂无资料'
  const prevention = String(
    raw.prevention || raw.preventionAdvice || ''
  ).trim() || '暂无防治建议'
  const plainSummary = packed.description || String(raw.description || '').replace(/【[^】]+】/g, '').trim()

  return {
    id: raw.id,
    name,
    image_url: raw.image_url || raw.imageUrl || raw.cover || img?.image_url || COVER,
    coverPos: raw.coverPos || img?.coverPos || POS[index % POS.length],
    description,
    causes,
    conditions,
    prevention,
    summary: plainSummary.slice(0, 56)
  }
}

/**
 * 仅映射后端列表，不再用本地假数据补齐
 * @param {object[]} apiList
 */
export function mapDiseaseList(apiList) {
  return (apiList || []).map((d, i) => normalizeDisease(d, i)).filter(Boolean)
}

/** @deprecated 请使用 mapDiseaseList */
export function fillDiseaseList(apiList) {
  return mapDiseaseList(apiList)
}

/**
 * 标准病害参考图：优先 API 字段，其次按中文名匹配，最后占位图
 * @param {{ name?: string, api?: object }} opts
 * @returns {string}
 */
export function resolveStandardDiseaseImage(opts = {}) {
  const api = opts.api || {}
  const fromApi =
    api.standard_image ||
    api.standardImage ||
    api.reference_image ||
    api.referenceImage ||
    api.disease_image ||
    api.diseaseImage
  if (fromApi && typeof fromApi === 'string') return fromApi

  const name = (opts.name || '').toString().trim()
  if (name) {
    const hit =
      resolveImageByName(name) ||
      DISEASE_IMAGES.find(
        (m) =>
          m.name === name ||
          name.includes(m.name) ||
          m.name.includes(name.replace(/（健康）$/, ''))
      )
    if (hit?.image_url) return hit.image_url
  }
  return COVER
}
