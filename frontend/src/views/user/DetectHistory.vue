<template>
  <div class="history-page">
    <header class="page-head">
      <div>
        <h1>我的诊断历史</h1>
        <p>识病与 AI 问诊记录，本地同步保存</p>
      </div>
      <div class="head-actions">
        <el-button
          v-if="list.length"
          plain
          class="btn-clear"
          @click="onClear"
        >
          清空历史
        </el-button>
        <el-button type="primary" class="btn-detect" @click="$router.push('/user/detect')">
          去拍照识病
        </el-button>
      </div>
    </header>

    <el-empty v-if="!list.length" description="暂无诊断记录">
      <el-button type="primary" class="btn-detect" @click="$router.push('/user/detect')">
        去拍照识病
      </el-button>
    </el-empty>

    <div v-else class="table-wrap">
      <el-table :data="list" stripe style="width: 100%">
        <el-table-column prop="createTime" label="时间" min-width="160" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag
              size="small"
              effect="light"
              :class="row.type === 'AI_CONSULT' ? 'tag-consult' : 'tag-detect'"
            >
              {{ typeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="crop" label="作物" min-width="90" />
        <el-table-column prop="diseaseName" label="病害" min-width="140" />
        <el-table-column label="置信度" width="100">
          <template #default="{ row }">
            {{ formatConfidence(row.confidence) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" class="link-detail" @click="openDetail(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="detailVisible"
      title="诊断详情"
      width="520px"
      destroy-on-close
      class="detail-dialog"
    >
      <template v-if="current">
        <div class="detail-grid">
          <div class="detail-row">
            <span class="label">类型</span>
            <span>{{ typeLabel(current.type) }}</span>
          </div>
          <div class="detail-row">
            <span class="label">时间</span>
            <span>{{ current.createTime }}</span>
          </div>
          <div class="detail-row">
            <span class="label">作物</span>
            <span>{{ current.crop || '—' }}</span>
          </div>
          <div class="detail-row">
            <span class="label">病害</span>
            <strong class="disease">{{ current.diseaseName }}</strong>
          </div>
          <div class="detail-row">
            <span class="label">置信度</span>
            <span class="conf">{{ formatConfidence(current.confidence) }}</span>
          </div>
          <div v-if="current.imageUrl" class="detail-img">
            <img :src="current.imageUrl" alt="诊断图片" />
          </div>
          <div class="detail-summary">
            <p class="label">摘要</p>
            <p class="summary-text">{{ current.summary || '暂无摘要' }}</p>
          </div>
        </div>
      </template>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="current?.type === 'DISEASE_DETECT'"
          type="primary"
          class="btn-detect"
          @click="goConsult"
        >
          去智能问诊
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useHistoryStore } from '@/store/history'

const historyStore = useHistoryStore()
const router = useRouter()

const list = computed(() => historyStore.getRecords())
const detailVisible = ref(false)
const current = ref(null)

function typeLabel(type) {
  return type === 'AI_CONSULT' ? '问诊' : '识别'
}

function formatConfidence(c) {
  const n = Number(c)
  if (Number.isNaN(n) || c == null || c === '') return '—'
  return `${(n <= 1 ? n * 100 : n).toFixed(1)}%`
}

function openDetail(row) {
  current.value = row
  detailVisible.value = true
}

function goConsult() {
  if (!current.value) return
  const conf = Number(current.value.confidence)
  const confStr = Number.isNaN(conf) ? '' : String(conf <= 1 ? conf : conf / 100)
  detailVisible.value = false
  router.push({
    path: '/ai-chat',
    query: {
      disease: current.value.diseaseName || '',
      confidence: confStr,
      image_url: current.value.imageUrl || '',
      crop: current.value.crop || ''
    }
  })
}

async function onClear() {
  try {
    await ElMessageBox.confirm('确定清空全部诊断历史？此操作不可恢复。', '清空确认', {
      confirmButtonText: '清空',
      cancelButtonText: '取消',
      type: 'warning'
    })
    historyStore.clearRecords()
    ElMessage.success('已清空诊断历史')
  } catch {
    /* cancelled */
  }
}

onMounted(() => {
  historyStore.getRecords()
})
</script>

<style scoped>
.history-page {
  max-width: 1100px;
  margin: 0 auto;
  padding-bottom: 40px;
}

.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.page-head h1 {
  margin: 0 0 6px;
  font-size: 1.45rem;
  color: #163b1f;
  font-weight: 750;
}
.page-head p {
  margin: 0;
  color: #6b7280;
  font-size: 0.9rem;
}

.head-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.btn-detect {
  --el-button-bg-color: #28b446;
  --el-button-border-color: #28b446;
  --el-button-hover-bg-color: #1f8f36;
  --el-button-hover-border-color: #1f8f36;
}
.btn-clear {
  --el-button-text-color: #6b7280;
  --el-button-border-color: #e5e7eb;
}

.table-wrap {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.05);
  padding: 8px 12px 16px;
  overflow: hidden;
}

.tag-detect {
  --el-tag-bg-color: #eaf8ee;
  --el-tag-border-color: #bbf7d0;
  --el-tag-text-color: #1f8f36;
}
.tag-consult {
  --el-tag-bg-color: #eff6ff;
  --el-tag-border-color: #bfdbfe;
  --el-tag-text-color: #2563eb;
}

.link-detail {
  --el-button-text-color: #28b446;
  --el-button-hover-text-color: #1f8f36;
}

.detail-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.detail-row {
  display: flex;
  gap: 12px;
  font-size: 0.92rem;
  color: #374151;
}
.detail-row .label {
  width: 64px;
  flex-shrink: 0;
  color: #94a3b8;
}
.disease {
  color: #14351c;
  font-weight: 700;
}
.conf {
  color: #28b446;
  font-weight: 650;
}
.detail-img {
  border-radius: 10px;
  overflow: hidden;
  background: #f3f4f6;
  max-height: 220px;
}
.detail-img img {
  display: block;
  width: 100%;
  max-height: 220px;
  object-fit: contain;
}
.detail-summary .label {
  margin: 0 0 6px;
  color: #94a3b8;
  font-size: 0.85rem;
}
.summary-text {
  margin: 0;
  padding: 12px 14px;
  background: #f8fafc;
  border-radius: 10px;
  color: #4b5563;
  line-height: 1.65;
  font-size: 0.9rem;
  white-space: pre-wrap;
  word-break: break-word;
}

:deep(.el-empty__description) {
  margin-bottom: 12px;
}
</style>
