/**
 * 资讯展示辅助：按标题补封面（不伪造资讯正文）
 */

const COVER = '/images/hero-farm.jpg'

const COVER_BY_TITLE = [
  { keys: ['春季田间管理'], cover: '/images/news/spring-field.png' },
  { keys: ['南方稻区', '稻瘟病早期监测'], cover: '/images/news/rice-blast-monitor.png' },
  { keys: ['智能识别', '基层农技服务'], cover: '/images/news/smart-diagnosis.png' },
  { keys: ['农技员认证', '认证通道'], cover: '/images/news/tech-cert.png' }
]

/**
 * @param {string} title
 * @returns {string|null}
 */
export function resolveNewsCoverByTitle(title) {
  const t = String(title || '')
  for (const rule of COVER_BY_TITLE) {
    if (rule.keys.some((k) => t.includes(k))) return rule.cover
  }
  return null
}

/**
 * 仅映射后端资讯列表
 * @param {object[]} apiList
 * @returns {object[]}
 */
export function resolveNewsList(apiList) {
  return (apiList || []).map((n) => {
    const byTitle = resolveNewsCoverByTitle(n.title)
    return {
      id: n.id,
      title: n.title,
      summary: n.summary || (n.content || '').slice(0, 72),
      content: n.content,
      cover: n.cover || byTitle || COVER,
      createTime: n.createTime,
      status: n.status
    }
  })
}
