/**
 * Markdown → 安全 HTML（marked + DOMPurify）
 * 用于 AI 问诊气泡富文本渲染
 */
import { marked } from 'marked'
import DOMPurify from 'dompurify'

marked.setOptions({
  gfm: true,
  breaks: true
})

/**
 * @param {string} text 原始 Markdown / 纯文本
 * @returns {string} 可安全用于 v-html 的 HTML
 */
export function renderMarkdown(text) {
  if (text == null || text === '') return ''
  const raw = String(text)
  if (raw.trim().toLowerCase() === 'null') return ''
  try {
    const html = marked.parse(raw, { async: false })
    return DOMPurify.sanitize(String(html), {
      USE_PROFILES: { html: true }
    })
  } catch {
    // 解析失败时按纯文本转义展示，避免 XSS
    return DOMPurify.sanitize(
      raw.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    )
  }
}

export default renderMarkdown
