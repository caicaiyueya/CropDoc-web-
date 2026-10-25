/**
 * ImgBB 免费图床上传（经 Vite 代理规避 CORS）
 *
 * 开发：POST /imgbb-api/1/upload  →  Vite 转发 → https://api.imgbb.com/1/upload
 * 生产构建无 Vite 代理时直连 https://api.imgbb.com/1/upload
 *
 * FormData：
 * - key   : ImgBB API Key（https://api.imgbb.com/ 免费申请）
 * - image : 图片文件
 *
 * 成功响应：{ success: true, data: { url: "https://i.ibb.co/..." } }
 */
import axios from 'axios'

/** 开发走代理；生产直连官方 API */
const IMGBB_UPLOAD_URL = import.meta.env.DEV
  ? '/imgbb-api/1/upload'
  : 'https://api.imgbb.com/1/upload'

/**
 * 上传到 ImgBB，返回公网图片 URL（data.url）
 * @param {File} file
 * @returns {Promise<string>}
 */
export async function uploadToImgBB(file) {
  if (!file) {
    throw new Error('未选择图片文件')
  }

  const apiKey = import.meta.env.VITE_IMGBB_API_KEY
  if (!apiKey || !String(apiKey).trim()) {
    throw new Error(
      '未配置 ImgBB API Key。请在 frontend/.env 中设置 VITE_IMGBB_API_KEY（免费申请：https://api.imgbb.com/）'
    )
  }

  const form = new FormData()
  form.append('key', String(apiKey).trim())
  // 官方字段名必须为 image
  form.append('image', file, file.name || 'leaf.jpg')

  try {
    const { data } = await axios.post(IMGBB_UPLOAD_URL, form, {
      timeout: 60000
      // 勿手动设置 Content-Type，由浏览器自动带 multipart boundary
    })

    return extractImgbbUrl(data)
  } catch (err) {
    if (err?.message && !err.response) {
      if (
        err.message.includes('ImgBB') ||
        err.message.includes('未选择') ||
        err.message.includes('未配置') ||
        err.message.includes('图床')
      ) {
        throw err
      }
    }
    const apiMsg =
      err.response?.data?.error?.message ||
      err.response?.data?.error ||
      err.response?.data?.status_txt ||
      err.message ||
      '图片上传失败'
    throw new Error(friendlyUploadError(String(apiMsg)))
  }
}

/** @deprecated 兼容旧调用名 */
export async function uploadImage(file) {
  return uploadToImgBB(file)
}

/**
 * @param {unknown} body ImgBB JSON
 * @returns {string}
 */
function extractImgbbUrl(body) {
  if (!body) {
    throw new Error('图床无响应')
  }
  if (body.success === false) {
    const msg = body.error?.message || body.status_txt || 'ImgBB 上传失败'
    throw new Error(msg)
  }
  const url = body?.data?.url || body?.data?.display_url
  if (typeof url === 'string' && url.startsWith('http')) {
    return url.trim()
  }
  throw new Error('图床响应中未找到 data.url')
}

function friendlyUploadError(msg) {
  const s = String(msg)
  if (/cors|network error|failed to fetch/i.test(s)) {
    return '图床上传失败（网络或跨域）。开发环境请确认 Vite 已配置 /imgbb-api 代理并已重启。'
  }
  if (/invalid.*key|api key|unauthorized|403/i.test(s)) {
    return 'ImgBB API Key 无效，请检查 frontend/.env 中的 VITE_IMGBB_API_KEY'
  }
  if (/413|too large|size|file too large/i.test(s)) {
    return '图片过大，请压缩后重试（ImgBB 免费额度有大小限制）'
  }
  return s.startsWith('图床') || s.startsWith('ImgBB') || s.startsWith('未配置')
    ? s
    : `图床上传失败：${s}`
}
