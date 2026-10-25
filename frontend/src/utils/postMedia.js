/**
 * 帖子图片 / 分类打包（images 字段：category||url1,url2）
 * 兼容旧数据：纯逗号分隔路径
 */

export const POST_CATEGORIES = [
  { value: 'experience', label: '经验分享' },
  { value: 'help', label: '病害求助' },
  { value: 'chat', label: '日常交流' }
]

export function categoryLabel(code) {
  return POST_CATEGORIES.find((c) => c.value === code)?.label || '交流'
}

/**
 * @param {string} category
 * @param {string[]} urls
 */
export function packPostImages(category, urls = []) {
  const clean = (urls || []).filter((u) => /^https?:\/\//i.test(String(u)))
  const cat = category || 'chat'
  if (!clean.length) return `${cat}||`
  return `${cat}||${clean.join(',')}`
}

/**
 * @param {string|null|undefined} images
 * @returns {{ category: string, urls: string[] }}
 */
export function unpackPostImages(images) {
  if (!images || !String(images).trim()) {
    return { category: 'chat', urls: [] }
  }
  const raw = String(images).trim()
  if (raw.includes('||')) {
    const idx = raw.indexOf('||')
    const cat = raw.slice(0, idx) || 'chat'
    const rest = raw.slice(idx + 2)
    const urls = rest
      ? rest.split(',').map((s) => s.trim()).filter((u) => /^https?:\/\//i.test(u) || u.startsWith('/'))
      : []
    return { category: cat, urls }
  }
  const urls = raw
    .split(',')
    .map((s) => s.trim())
    .filter((u) => /^https?:\/\//i.test(u) || u.startsWith('/'))
  return { category: 'chat', urls }
}
