/**
 * 农作物病虫害识别 — 直连云托管 AI 服务
 *
 * 注意：
 * 1. 此实例与业务后端 /api 分离，不走 JWT，不经过 request.js。
 * 2. 若浏览器控制台出现 CORS / 跨域报错，属于后端未配置 Access-Control-Allow-Origin，
 *    无需改前端业务逻辑；此处拦截器只负责把错误转成可读文案。
 * 3. timeout 固定 60s：冷启动与模型推理可能较慢。
 */
import axios from 'axios'

/** 生产环境直连云托管；开发环境走 Vite 代理 /ai-proxy，减轻本地 CORS */
export const AI_PREDICT_BASE = import.meta.env.DEV
  ? '/ai-proxy'
  : 'https://ai-service-267301-4-1440848964.sh.run.tcloudbase.com'

const aiHttp = axios.create({
  baseURL: AI_PREDICT_BASE,
  timeout: 60000, // 60 秒，满足冷启动 / 推理耗时
  headers: {
    'Content-Type': 'application/json'
  }
})

/** 响应成功直接返回 data；失败统一抛出带 message 的 Error */
aiHttp.interceptors.response.use(
  (response) => response.data,
  (error) => {
    // CORS 失败时往往没有 response，只有 Network Error
    if (!error.response) {
      if (error.code === 'ECONNABORTED' || String(error.message).includes('timeout')) {
        return Promise.reject(new Error('识别超时（超过 60 秒），请稍后重试'))
      }
      // 常见：跨域被浏览器拦截，或服务未启动
      return Promise.reject(
        new Error(
          '网络请求失败。若控制台提示 CORS/跨域，请在后端开启允许前端域名；否则请检查服务是否在线。'
        )
      )
    }
    const status = error.response.status
    const detail =
      error.response.data?.detail ||
      error.response.data?.message ||
      error.message ||
      '识别服务异常'
    return Promise.reject(new Error(`[${status}] ${detail}`))
  }
)

/**
 * 调用 /predict
 * @param {{ image_url: string, crop?: string }} payload
 * @returns {Promise<{ disease_key?: string, disease?: string, confidence: number }>}
 */
export function predictDisease(payload) {
  if (!payload?.image_url) {
    return Promise.reject(new Error('image_url 不能为空'))
  }
  // POST JSON：{ image_url, crop? }
  return aiHttp.post('/predict', {
    image_url: payload.image_url,
    ...(payload.crop ? { crop: payload.crop } : {})
  })
}

export default aiHttp
