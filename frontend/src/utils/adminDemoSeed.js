/**
 * 管理端启动时调用 /admin/demo/sync，将病害库 / 资讯 / 社区演示种子写入数据库。
 * 前后端展示一律读接口，不再使用本地 mock 兜底。
 */
import { syncDemoContent } from '@/api/admin'

let syncPromise = null

/** 同一会话内成功同步只跑一次；失败可重试 */
export async function ensureDemoSynced() {
  if (!syncPromise) {
    syncPromise = syncDemoContent()
      .then(() => true)
      .catch(() => {
        syncPromise = null
        return false
      })
  }
  return syncPromise
}
