<template>
  <div class="detect-page">
    <header class="page-head">
      <h1>病虫害识别</h1>
      <p>选择作物并上传叶片照片，右侧动态展示识别结果</p>
    </header>

    <!-- 左 25% / 中 50% / 右 25%，Flex 等高，Gap 24px -->
    <div class="workbench">
      <!-- 左：作物选择 -->
      <aside class="panel panel-left">
        <h3 class="panel-title">选择作物</h3>
        <el-input
          v-model="cropQuery"
          class="crop-search"
          clearable
          placeholder="搜索作物"
          :prefix-icon="Search"
        />
        <div class="crop-scroll">
          <div v-for="group in filteredGroups" :key="group.category" class="crop-group">
            <p class="group-label">{{ group.category }}</p>
            <div class="tag-cloud">
              <button
                v-for="c in group.items"
                :key="c.value"
                type="button"
                class="crop-tag"
                :class="{ active: selectedCrop === c.value }"
                :disabled="busy"
                @click="selectedCrop = c.value"
              >
                <el-icon v-if="selectedCrop === c.value" class="tag-check" :size="12">
                  <Check />
                </el-icon>
                {{ c.label }}
              </button>
            </div>
          </div>
          <el-empty
            v-if="!filteredGroups.length"
            description="未找到匹配作物"
            :image-size="48"
          />
        </div>
      </aside>

      <!-- 中：上传工作区 -->
      <section class="panel panel-center">
        <div class="tips-alert" role="note">
          <div class="tips-title">
            <el-icon class="tips-cam" :size="15"><Camera /></el-icon>
            <strong>拍摄建议</strong>
          </div>
          <span class="tips-divider" aria-hidden="true" />
          <p class="tips-items">占屏60% · 光线充足 · 病斑清晰 · 限10M</p>
        </div>

        <el-upload
          class="uploader"
          drag
          multiple
          :auto-upload="false"
          :show-file-list="false"
          accept="image/jpeg,image/png,image/gif,image/webp"
          :disabled="busy"
          :on-change="onFileChange"
        >
          <div class="upload-empty">
            <el-icon class="upload-cam" :size="52"><Camera /></el-icon>
            <p class="main-tip">点击或拖拽图片至此</p>
            <p class="sub-tip">支持 JPEG / PNG / GIF / WebP</p>
          </div>
        </el-upload>

        <div v-if="uploads.length" class="thumb-list">
          <div
            v-for="item in uploads"
            :key="item.id"
            class="thumb-item"
            :class="{ active: item.id === activeUploadId }"
            @click="!busy && (activeUploadId = item.id)"
          >
            <img :src="item.url" :alt="item.name" />
            <button
              type="button"
              class="thumb-remove"
              title="删除"
              :disabled="busy"
              @click.stop="removeUpload(item.id)"
            >
              ×
            </button>
          </div>
        </div>

        <div class="btn-row">
          <button
            type="button"
            class="btn-detect"
            :disabled="!canDetect || busy"
            @click="onPredict"
          >
            <span v-if="busy" class="btn-spin" />
            {{ buttonLabel }}
          </button>
        </div>
      </section>

      <!-- 右：动态结果 -->
      <aside class="panel panel-right">
        <Transition name="panel-fade" mode="out-in">
          <!-- A 空态 -->
          <div v-if="panelStatus === 'idle'" key="idle" class="dyn-inner empty-state">
            <div class="empty-illust" aria-hidden="true">
              <svg viewBox="0 0 96 96" fill="none">
                <rect x="16" y="22" width="64" height="50" rx="10" stroke="#cbd5e1" stroke-width="2.5" />
                <circle cx="36" cy="42" r="7" stroke="#cbd5e1" stroke-width="2.5" />
                <path d="M16 60l16-14 14 10 10-7 24 18" stroke="#cbd5e1" stroke-width="2.5" stroke-linejoin="round" />
                <circle cx="68" cy="34" r="12" fill="#f1f5f9" stroke="#94a3b8" stroke-width="2" />
                <path d="M68 29v10M63 34h10" stroke="#94a3b8" stroke-width="2" stroke-linecap="round" />
              </svg>
            </div>
            <p class="empty-title">等待上传图片进行识别</p>
            <p class="empty-sub">完成上传后点击「开始识别」</p>
          </div>

          <!-- B 加载态 -->
          <div v-else-if="panelStatus === 'loading'" key="loading" class="dyn-inner loading-state">
            <p class="loading-title">{{ loadingTitle }}</p>
            <el-skeleton animated :rows="5" />
            <div class="loading-dots" aria-hidden="true">
              <span /><span /><span />
            </div>
          </div>

          <!-- C 完成态：中文名 + 危害等级 + 对比图 + 防治要点 -->
          <div v-else key="success" class="dyn-inner result-state">
            <div class="result-top">
              <h2 class="disease-name">{{ result.diseaseCn }}</h2>
              <span class="sev-badge" :class="severityClass">{{ severityLabel }}</span>
            </div>

            <div class="compare">
              <div class="img-col">
                <p class="img-label">用户上传图</p>
                <div class="img-frame">
                  <img v-if="userPreviewUrl" :src="userPreviewUrl" alt="用户上传" />
                  <div v-else class="img-fallback">暂无预览</div>
                </div>
              </div>
              <div class="img-col">
                <p class="img-label">标准病害图</p>
                <div class="img-frame ref">
                  <img :src="standardImageUrl" alt="标准病害参考" />
                  <span class="ref-badge">参考样例</span>
                </div>
              </div>
            </div>

            <div class="advice">
              <p class="advice-title">防治要点</p>
              <p class="advice-text">{{ result.preventionTips }}</p>
            </div>

            <div class="conf-block">
              <div class="conf-top">
                <span>置信度</span>
                <strong>{{ result.confidenceText }}</strong>
              </div>
              <el-progress
                :percentage="confidencePct"
                :stroke-width="8"
                :show-text="false"
                color="#28B446"
              />
            </div>

            <div class="result-actions">
              <el-button type="primary" round class="btn-diagnose" @click="goAiChat">
                立即智能诊断
              </el-button>
              <button type="button" class="reset-link" @click="resetPanel">重新识别</button>
            </div>
          </div>
        </Transition>
      </aside>
    </div>
  </div>
</template>

<script setup>
/**
 * 真实链路（不变）：
 * 1) 本地选图预览  2) ImgBB 上传拿公网 URL  3) POST AI /predict  4) 右侧展示结果
 */
import { computed, onBeforeUnmount, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Camera, Check, Search } from '@element-plus/icons-vue'
import { predictDisease } from '@/api/aiPredict'
import { useHistoryStore } from '@/store/history'
import { resolveStandardDiseaseImage } from '@/utils/diseaseCatalog'
import { uploadToImgBB } from '@/utils/uploadImage'
import { getPreventionTips, toDiseaseCn } from '@/utils/diseaseMap'

const router = useRouter()
const historyStore = useHistoryStore()

/** 模型 crop 前缀与中文展示名；按四类分组 */
const CROP_GROUPS = [
  {
    category: '粮食作物',
    items: [
      { label: '水稻', value: 'Rice' },
      { label: '小麦', value: 'Wheat' },
      { label: '玉米', value: 'Corn' },
      { label: '大豆', value: 'Soybean' }
    ]
  },
  {
    category: '蔬菜作物',
    items: [
      { label: '番茄', value: 'Tomato' },
      { label: '马铃薯', value: 'Potato' },
      { label: '辣椒', value: 'Chili' },
      { label: '甜椒', value: 'Pepper_bell' },
      { label: '黄瓜', value: 'Cucumber' }
    ]
  },
  {
    category: '经济作物',
    items: [
      { label: '茶', value: 'Tea' },
      { label: '咖啡', value: 'Coffee' },
      { label: '甘蔗', value: 'Sugarcane' },
      { label: '木薯', value: 'Cassava' }
    ]
  },
  {
    category: '林木果树',
    items: [
      { label: '苹果', value: 'Apple' },
      { label: '葡萄', value: 'Grape' },
      { label: '草莓', value: 'Strawberry' },
      { label: '樱桃', value: 'Cherry' },
      { label: '桃子', value: 'Peach' },
      { label: '芒果', value: 'Mango' },
      { label: '柠檬', value: 'Lemon' },
      { label: '石榴', value: 'Pomegranate' },
      { label: '番石榴', value: 'Gauva' },
      { label: '枣', value: 'Jamun' }
    ]
  }
]

const MAX_UPLOADS = 3

const cropQuery = ref('')
const selectedCrop = ref('')
const uploads = ref([])
const activeUploadId = ref('')
let uidSeq = 0

/** idle | uploading | predicting */
const phase = ref('idle')
const result = ref(null)

const busy = computed(() => phase.value === 'uploading' || phase.value === 'predicting')
const canDetect = computed(() => uploads.value.length > 0)

/** idle | loading | success — 右侧面板状态 */
const panelStatus = computed(() => {
  if (busy.value) return 'loading'
  if (result.value) return 'success'
  return 'idle'
})

const loadingTitle = computed(() => {
  if (phase.value === 'uploading') return '正在上传图片…'
  return 'AI 正在诊断中...'
})

const buttonLabel = computed(() => {
  if (phase.value === 'uploading') return '上传中…'
  if (phase.value === 'predicting') return '识别中…'
  return '开始识别'
})

const filteredGroups = computed(() => {
  const q = cropQuery.value.trim().toLowerCase()
  if (!q) return CROP_GROUPS
  return CROP_GROUPS.map((g) => ({
    ...g,
    items: g.items.filter(
      (c) => c.label.toLowerCase().includes(q) || c.value.toLowerCase().includes(q)
    )
  })).filter((g) => g.items.length)
})

const activeUpload = computed(
  () => uploads.value.find((u) => u.id === activeUploadId.value) || uploads.value[0] || null
)

const confidencePct = computed(() => {
  const c = Number(result.value?.confidence)
  if (Number.isNaN(c)) return 0
  return Math.min(100, Math.max(0, +(c <= 1 ? c * 100 : c).toFixed(1)))
})

/** 左侧对比：当前预览图优先，其次公网上传地址 */
const userPreviewUrl = computed(
  () => activeUpload.value?.url || result.value?.publicUrl || ''
)

/** 右侧标准图：API / 病害库映射 / 占位 */
const standardImageUrl = computed(
  () =>
    result.value?.standardImage ||
    resolveStandardDiseaseImage({ name: result.value?.diseaseCn })
)

/** >80% 重度；50%-80% 中度；低于 50% 轻度 */
const severityLabel = computed(() => {
  const v = confidencePct.value
  if (v > 80) return '重度'
  if (v >= 50) return '中度'
  return '轻度'
})

const severityClass = computed(() => {
  if (severityLabel.value === '重度') return 'severe'
  if (severityLabel.value === '中度') return 'medium'
  return 'mild'
})

function onFileChange(uploadFile) {
  const raw = uploadFile.raw
  if (!raw || busy.value) return
  if (!raw.type.startsWith('image/')) {
    ElMessage.error('请上传图片文件')
    return
  }
  if (raw.size > 10 * 1024 * 1024) {
    ElMessage.error('图片不能超过 10MB')
    return
  }
  if (uploads.value.length >= MAX_UPLOADS) {
    ElMessage.warning(`最多上传 ${MAX_UPLOADS} 张图片`)
    return
  }
  const id = `u-${Date.now()}-${uidSeq++}`
  uploads.value.push({
    id,
    file: raw,
    url: URL.createObjectURL(raw),
    name: raw.name
  })
  activeUploadId.value = id
  result.value = null
}

function removeUpload(id) {
  if (busy.value) return
  const idx = uploads.value.findIndex((u) => u.id === id)
  if (idx < 0) return
  URL.revokeObjectURL(uploads.value[idx].url)
  uploads.value.splice(idx, 1)
  if (activeUploadId.value === id) {
    activeUploadId.value = uploads.value[0]?.id || ''
  }
  if (!uploads.value.length) result.value = null
}

function resetPanel() {
  result.value = null
}

function cropLabel(value) {
  if (!value) return '未指定'
  for (const g of CROP_GROUPS) {
    const found = g.items.find((c) => c.value === value)
    if (found) return found.label
  }
  return value
}

function normalizeResult(data, publicUrl) {
  const diseaseKey = data?.disease_key || data?.disease || data?.result || '未知'
  let conf = Number(data?.confidence)
  if (Number.isNaN(conf)) conf = 0
  const pct = conf <= 1 ? conf * 100 : conf
  const diseaseCn = toDiseaseCn(diseaseKey)
  const preventionTips =
    data?.prevention ||
    data?.prevention_tips ||
    data?.advice ||
    getPreventionTips(diseaseKey)
  return {
    diseaseKey,
    diseaseCn,
    confidenceText: `${pct.toFixed(1)}%`,
    confidence: conf,
    publicUrl,
    crop: selectedCrop.value || '',
    preventionTips,
    /** 供对比区绑定；优先接口标准图字段 */
    standardImage: resolveStandardDiseaseImage({ name: diseaseCn, api: data || {} })
  }
}

/** 跳转 AI 问诊：disease / confidence / image_url */
function goAiChat() {
  if (!result.value) return
  const conf = Number(result.value.confidence)
  const confStr = Number.isNaN(conf)
    ? ''
    : String(conf <= 1 ? conf : conf / 100)
  router.push({
    path: '/ai-chat',
    query: {
      disease: result.value.diseaseCn || toDiseaseCn(result.value.diseaseKey),
      confidence: confStr,
      image_url: result.value.publicUrl || '',
      crop: cropLabel(result.value.crop || selectedCrop.value)
    }
  })
}

async function onPredict() {
  const current = activeUpload.value
  if (busy.value || !current) {
    ElMessage.warning('请先上传至少一张图片')
    return
  }

  result.value = null
  let publicUrl = ''

  try {
    phase.value = 'uploading'
    publicUrl = await uploadToImgBB(current.file)
    if (!/^https?:\/\//i.test(publicUrl)) {
      throw new Error('图床返回的地址无效')
    }

    phase.value = 'predicting'
    const data = await predictDisease({
      image_url: publicUrl,
      crop: selectedCrop.value || undefined
    })

    result.value = normalizeResult(data, publicUrl)
    historyStore.addRecord({
      type: 'DISEASE_DETECT',
      crop: cropLabel(result.value.crop || selectedCrop.value),
      diseaseName: result.value.diseaseCn,
      confidence: result.value.confidence,
      imageUrl: publicUrl,
      summary: result.value.preventionTips || ''
    })
    ElMessage.success('识别完成')
  } catch (err) {
    console.error('[AiPredict]', err)
    ElMessage.error(err?.message || '识别失败，请稍后重试')
  } finally {
    phase.value = 'idle'
  }
}

onBeforeUnmount(() => {
  uploads.value.forEach((u) => URL.revokeObjectURL(u.url))
})
</script>

<style scoped>
.detect-page {
  max-width: 1440px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding-bottom: 40px;
  min-height: calc(100vh - 140px);
}

.page-head h1 {
  margin: 0 0 6px;
  font-size: 1.5rem;
  color: #163b1f;
  font-weight: 750;
}
.page-head p {
  margin: 0;
  color: #6b7280;
  font-size: 0.92rem;
}

.workbench {
  display: flex;
  align-items: stretch;
  gap: 24px;
  flex: 1;
  min-height: 600px;
}

.panel {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.05);
  padding: 22px 20px;
  display: flex;
  flex-direction: column;
  min-height: 600px;
  box-sizing: border-box;
}

.panel-left {
  flex: 0 0 25%;
  max-width: 25%;
  min-width: 0;
}
.panel-center {
  flex: 0 0 50%;
  max-width: 50%;
  min-width: 0;
  gap: 14px;
}
.panel-right {
  flex: 0 0 25%;
  max-width: 25%;
  min-width: 0;
  padding: 0;
  background: transparent;
  box-shadow: none;
  overflow: hidden;
}

.panel-title {
  margin: 0 0 14px;
  font-size: 1.05rem;
  color: #163b1f;
  font-weight: 700;
  flex-shrink: 0;
}

.crop-search {
  margin-bottom: 16px;
  flex-shrink: 0;
}
.crop-search :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #e5e7eb inset;
}
.crop-search :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #28b446 inset !important;
}

.crop-scroll {
  flex: 1;
  overflow: auto;
  min-height: 0;
}
.crop-group + .crop-group {
  margin-top: 20px;
}
.group-label {
  margin: 0 0 10px;
  font-size: 0.75rem;
  font-weight: 700;
  color: #94a3b8;
  letter-spacing: 0.04em;
}
.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.crop-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: 1px solid #e5e7eb;
  background: #fff;
  color: #6b7280;
  border-radius: 999px;
  padding: 8px 14px;
  font-size: 0.88rem;
  cursor: pointer;
  transition: 0.15s ease;
}
.crop-tag:hover:not(:disabled) {
  border-color: #86d89a;
  color: #28b446;
}
.crop-tag.active {
  background: #eaf8ee;
  border-color: #28b446;
  color: #1f8f36;
  font-weight: 700;
}
.crop-tag:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.tag-check {
  color: #28b446;
}

.tips-alert {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  box-sizing: border-box;
  padding: 10px 14px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 10px;
}
.tips-title {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.tips-cam {
  color: #28b446;
}
.tips-title strong {
  font-size: 13px;
  font-weight: 700;
  color: #111827;
  white-space: nowrap;
}
.tips-divider {
  flex-shrink: 0;
  width: 1px;
  height: 14px;
  background: #d1d5db;
}
.tips-items {
  margin: 0;
  flex: 1;
  min-width: 0;
  font-size: 12px;
  color: #4b5563;
  line-height: 1.4;
  white-space: nowrap;
}

.uploader {
  flex: 1;
  min-height: 0;
}
.uploader :deep(.el-upload),
.uploader :deep(.el-upload-dragger) {
  width: 100%;
  height: 100%;
  border-radius: 12px;
  border: 2px dashed #c9e8d1;
  background: #f8fcf9;
}
.uploader :deep(.el-upload) {
  height: 100%;
}
.uploader :deep(.el-upload-dragger) {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 0;
  padding: 24px;
}
.uploader :deep(.el-upload-dragger:hover) {
  border-color: #28b446;
  background: #f0faf3;
}
.upload-empty {
  text-align: center;
}
.upload-cam {
  color: #86d89a;
  margin-bottom: 12px;
}
.main-tip {
  margin: 0 0 8px;
  font-size: 1.08rem;
  color: #374151;
  font-weight: 600;
}
.sub-tip {
  margin: 0;
  font-size: 0.85rem;
  color: #9ca3af;
}

.thumb-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  flex-shrink: 0;
}
.thumb-item {
  position: relative;
  width: 76px;
  height: 76px;
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid #e5e7eb;
  cursor: pointer;
  background: #f3f4f6;
}
.thumb-item.active {
  border-color: #28b446;
  box-shadow: 0 0 0 3px rgba(40, 180, 70, 0.16);
}
.thumb-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.thumb-remove {
  position: absolute;
  top: 3px;
  right: 3px;
  width: 20px;
  height: 20px;
  border: none;
  border-radius: 50%;
  background: #dc2626;
  color: #fff;
  font-size: 13px;
  line-height: 1;
  cursor: pointer;
  display: grid;
  place-items: center;
  padding: 0;
}
.thumb-remove:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-row {
  flex-shrink: 0;
  display: flex;
  justify-content: center;
}
.btn-detect {
  width: 100%;
  max-width: 360px;
  border: none;
  border-radius: 999px;
  padding: 14px 28px;
  font-size: 1.05rem;
  font-weight: 700;
  color: #fff;
  background: #28b446;
  cursor: pointer;
  box-shadow: 0 10px 24px rgba(40, 180, 70, 0.28);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.btn-detect:hover:not(:disabled) {
  background: #1f8f36;
}
.btn-detect:disabled {
  opacity: 0.42;
  cursor: not-allowed;
  box-shadow: none;
}
.btn-spin {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.35);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.dyn-inner {
  height: 100%;
  min-height: 600px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.05);
  padding: 24px 20px;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.empty-state {
  background: #fafbfc;
  align-items: center;
  justify-content: center;
  text-align: center;
}
.empty-illust {
  width: 96px;
  height: 96px;
  margin-bottom: 18px;
}
.empty-illust svg {
  width: 100%;
  height: 100%;
}
.empty-title {
  margin: 0 0 8px;
  font-size: 0.98rem;
  font-weight: 700;
  color: #64748b;
}
.empty-sub {
  margin: 0;
  font-size: 0.82rem;
  color: #94a3b8;
}

.loading-title {
  margin: 0 0 16px;
  font-size: 1.05rem;
  font-weight: 700;
  color: #163b1f;
}
.loading-dots {
  margin-top: auto;
  display: flex;
  justify-content: center;
  gap: 8px;
}
.loading-dots span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #86d89a;
  animation: bounce 1.2s ease-in-out infinite;
}
.loading-dots span:nth-child(2) {
  animation-delay: 0.15s;
}
.loading-dots span:nth-child(3) {
  animation-delay: 0.3s;
}
@keyframes bounce {
  0%,
  80%,
  100% {
    transform: scale(0.7);
    opacity: 0.45;
  }
  40% {
    transform: scale(1.15);
    opacity: 1;
  }
}

.result-state {
  gap: 0;
}
.result-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
  flex-shrink: 0;
}
.disease-name {
  margin: 0;
  flex: 1;
  min-width: 0;
  font-size: clamp(1.2rem, 1.6vw, 1.5rem);
  line-height: 1.3;
  color: #14351c;
  font-weight: 800;
  word-break: break-word;
}
.btn-diagnose {
  width: 100%;
  --el-button-bg-color: #28b446;
  --el-button-border-color: #28b446;
  --el-button-hover-bg-color: #1f8f36;
  --el-button-hover-border-color: #1f8f36;
}
.sev-badge {
  flex-shrink: 0;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 0.78rem;
  font-weight: 700;
}
.sev-badge.severe {
  background: #fef2f2;
  color: #dc2626;
}
.sev-badge.medium {
  background: #fff7ed;
  color: #ea580c;
}
.sev-badge.mild {
  background: #f0fdf4;
  color: #16a34a;
}

.compare {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-bottom: 14px;
  flex-shrink: 0;
}
.img-col {
  min-width: 0;
}
.img-label {
  margin: 0 0 6px;
  font-size: 0.72rem;
  font-weight: 650;
  color: #94a3b8;
  text-align: center;
}
.img-frame {
  position: relative;
  aspect-ratio: 1;
  border-radius: 10px;
  overflow: hidden;
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
}
.img-frame img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.img-frame.ref {
  border-color: #c9e8d1;
}
.img-fallback {
  height: 100%;
  display: grid;
  place-items: center;
  font-size: 0.72rem;
  color: #94a3b8;
}
.ref-badge {
  position: absolute;
  left: 6px;
  bottom: 6px;
  padding: 2px 7px;
  border-radius: 6px;
  font-size: 0.62rem;
  font-weight: 700;
  color: #166534;
  background: rgba(240, 253, 244, 0.92);
}

.conf-block {
  margin-bottom: 12px;
  flex-shrink: 0;
}
.conf-top {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
  font-size: 0.68rem;
  color: #94a3b8;
}
.conf-top strong {
  color: #28b446;
  font-size: 0.72rem;
  font-weight: 600;
}

.advice {
  flex: 1;
  background: #f3f4f6;
  border-radius: 12px;
  padding: 12px 14px;
  margin-bottom: 12px;
  min-height: 0;
  overflow: auto;
}
.advice-title {
  margin: 0 0 6px;
  font-size: 0.82rem;
  font-weight: 700;
  color: #374151;
}
.advice-text {
  margin: 0;
  font-size: 0.82rem;
  line-height: 1.65;
  color: #6b7280;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.pub-url {
  margin: 10px 0 0;
  font-size: 0.72rem;
  color: #94a3b8;
  word-break: break-all;
  line-height: 1.4;
}
.pub-url a {
  color: #28b446;
}

.result-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
}
.result-actions .el-button {
  width: 100%;
  margin: 0;
}
.reset-link {
  border: none;
  background: none;
  color: #6f9e7c;
  font-size: 0.82rem;
  font-weight: 600;
  cursor: pointer;
  padding: 6px 0 0;
}
.reset-link:hover {
  color: #28b446;
  text-decoration: underline;
}

.panel-fade-enter-active,
.panel-fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}
.panel-fade-enter-from {
  opacity: 0;
  transform: translateY(12px);
}
.panel-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

@media (max-width: 1100px) {
  .workbench {
    flex-direction: column;
    gap: 16px;
  }
  .panel-left,
  .panel-center,
  .panel-right {
    flex: none;
    max-width: none;
    width: 100%;
    min-height: auto;
  }
  .dyn-inner {
    min-height: 300px;
  }
  .uploader :deep(.el-upload-dragger) {
    min-height: 200px;
  }
  .crop-scroll {
    max-height: 200px;
  }
}
</style>
