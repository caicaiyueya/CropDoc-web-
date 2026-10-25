<template>
  <div class="detect-page">
    <header class="page-head">
      <h1>病虫害识别</h1>
      <p>选择作物并上传叶片照片，右侧动态展示识别结果</p>
    </header>

    <!-- 左 25% / 中 50% / 右 25%，Flex 等高，Gap 24px -->
    <div class="workbench">
      <!-- 1. 左栏：作物选择 -->
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
                :key="c.name"
                type="button"
                class="crop-tag"
                :class="{ active: selectedCrop === c.name }"
                @click="selectCrop(c)"
              >
                <el-icon v-if="selectedCrop === c.name" class="tag-check" :size="12">
                  <Check />
                </el-icon>
                {{ c.name }}
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

      <!-- 2. 中栏：上传工作区 -->
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
          :on-change="onFileChange"
        >
          <div class="upload-empty">
            <el-icon class="upload-cam" :size="52"><Camera /></el-icon>
            <p class="main-tip">点击或拖拽图片至此</p>
            <p class="sub-tip">支持 JPEG / PNG / GIF / WebP</p>
          </div>
        </el-upload>

        <div class="thumb-list" :class="{ empty: !uploads.length }">
          <div
            v-for="item in uploads"
            :key="item.id"
            class="thumb-item"
            :class="{ active: item.id === activeUploadId }"
            @click="activeUploadId = item.id"
          >
            <img :src="item.url" :alt="item.name" />
            <button
              type="button"
              class="thumb-remove"
              title="删除"
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
            :disabled="!canDetect || panelStatus === 'loading'"
            @click="onDetect"
          >
            <span v-if="panelStatus === 'loading'" class="btn-spin" />
            {{ panelStatus === 'loading' ? '识别中…' : '开始识别' }}
          </button>
        </div>
      </section>

      <!-- 3. 右栏：动态结果 -->
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
            <p class="loading-title">AI 正在诊断中...</p>
            <el-skeleton animated :rows="5" />
            <div class="loading-dots" aria-hidden="true">
              <span /><span /><span />
            </div>
          </div>

          <!-- C 完成态 -->
          <div v-else key="success" class="dyn-inner result-state">
            <div class="result-top">
              <h2 class="disease-name">{{ resultData.result || '未知病害' }}</h2>
              <span class="sev-badge" :class="severityClass">{{ severityLabel }}</span>
            </div>
            <div class="conf-block">
              <div class="conf-top">
                <span>置信度</span>
                <strong>{{ confidenceText }}</strong>
              </div>
              <el-progress
                :percentage="confidencePct"
                :stroke-width="12"
                :show-text="false"
                color="#28B446"
              />
            </div>
            <div class="advice">
              <p class="advice-title">防治建议</p>
              <p class="advice-text">
                {{
                  resultData.description ||
                  '建议结合田间症状进一步确认，并可进入智能问诊获取详细防治方案。'
                }}
              </p>
            </div>
            <div class="result-actions">
              <el-button type="primary" round @click="goFullResult">查看详情</el-button>
              <el-button round @click="goDiagnosis">智能问诊</el-button>
              <button type="button" class="reset-link" @click="resetPanel">重新识别</button>
            </div>
          </div>
        </Transition>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Camera, Check, Search } from '@element-plus/icons-vue'
import { cropList } from '@/api/content'
import { detectDisease, uploadFile } from '@/api/detection'
import { useHistoryStore } from '@/store/history'

/** 设计稿分组：粮食 / 经济 */
const CROP_GROUPS = [
  { category: '粮食作物', names: ['水稻', '小麦', '玉米'] },
  { category: '经济作物', names: ['番茄', '黄瓜', '草莓'] }
]

const MAX_UPLOADS = 3

const router = useRouter()
const historyStore = useHistoryStore()
const apiCrops = ref([])
const cropQuery = ref('')
const selectedCrop = ref('')
const selectedCropId = ref(null)

const uploads = ref([])
const activeUploadId = ref('')
let uidSeq = 0

/** idle | loading | success */
const panelStatus = ref('idle')
const resultData = ref(null)

/** 未传图时置灰；有图即可识别（作物可选） */
const canDetect = computed(() => uploads.value.length > 0)

const activeUpload = computed(
  () => uploads.value.find((u) => u.id === activeUploadId.value) || uploads.value[0] || null
)

const groupedCrops = computed(() => {
  const apiMap = new Map((apiCrops.value || []).map((c) => [c.name, c]))
  const used = new Set()
  const groups = CROP_GROUPS.map((g) => ({
    category: g.category,
    items: g.names.map((name) => {
      used.add(name)
      return { id: apiMap.get(name)?.id ?? null, name }
    })
  }))
  const extras = (apiCrops.value || [])
    .filter((c) => !used.has(c.name))
    .map((c) => ({ id: c.id, name: c.name }))
  if (extras.length) groups.push({ category: '其他', items: extras })
  return groups.filter((g) => g.items.length)
})

const filteredGroups = computed(() => {
  const q = cropQuery.value.trim().toLowerCase()
  if (!q) return groupedCrops.value
  return groupedCrops.value
    .map((g) => ({
      ...g,
      items: g.items.filter((c) => c.name.toLowerCase().includes(q))
    }))
    .filter((g) => g.items.length)
})

const confidencePct = computed(() => {
  const c = Number(resultData.value?.confidence)
  if (Number.isNaN(c)) return 0
  return Math.min(100, Math.max(0, +(c <= 1 ? c * 100 : c).toFixed(1)))
})

const confidenceText = computed(() =>
  resultData.value ? `${confidencePct.value.toFixed(1)}%` : '--'
)

const severityLabel = computed(() => {
  const v = confidencePct.value / 100
  if (v >= 0.85) return '重度'
  if (v >= 0.6) return '中度'
  return '轻度'
})

const severityClass = computed(() => {
  if (severityLabel.value === '重度') return 'severe'
  if (severityLabel.value === '中度') return 'medium'
  return 'mild'
})

onMounted(async () => {
  try {
    const res = await cropList()
    apiCrops.value = res.data || []
  } catch {
    apiCrops.value = []
  }
})

onBeforeUnmount(() => {
  uploads.value.forEach((u) => URL.revokeObjectURL(u.url))
})

function selectCrop(c) {
  selectedCrop.value = c.name
  selectedCropId.value = c.id ?? null
}

function onFileChange(uploadFile) {
  const raw = uploadFile.raw
  if (!raw) return
  const okTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'image/jpg']
  if (!okTypes.includes(raw.type)) {
    ElMessage.error('仅支持 JPEG / PNG / GIF / WebP')
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
    name: raw.name,
    path: ''
  })
  activeUploadId.value = id
  if (panelStatus.value === 'success') panelStatus.value = 'idle'
}

function removeUpload(id) {
  const idx = uploads.value.findIndex((u) => u.id === id)
  if (idx < 0) return
  URL.revokeObjectURL(uploads.value[idx].url)
  uploads.value.splice(idx, 1)
  if (activeUploadId.value === id) {
    activeUploadId.value = uploads.value[0]?.id || ''
  }
  if (!uploads.value.length && panelStatus.value !== 'loading') {
    panelStatus.value = 'idle'
    resultData.value = null
  }
}

function resetPanel() {
  panelStatus.value = 'idle'
  resultData.value = null
}

async function onDetect() {
  const current = activeUpload.value
  if (!current) {
    ElMessage.warning('请先上传至少一张图片')
    return
  }
  panelStatus.value = 'loading'
  resultData.value = null
  try {
    if (!current.path) {
      const up = await uploadFile(current.file)
      current.path = up.data.path
    }
    const res = await detectDisease({
      imagePath: current.path,
      cropId: selectedCropId.value || null
    })
    const payload = {
      ...res.data,
      previewUrl: current.url,
      cropName: selectedCrop.value || ''
    }
    resultData.value = payload
    sessionStorage.setItem('ynzz_last_detect', JSON.stringify(payload))
    // 同步写入本地历史；后端 detection_record 已由接口落库（含 image_path）
    historyStore.addRecord({
      id: payload.id != null ? `api-${payload.id}` : undefined,
      type: 'DISEASE_DETECT',
      crop: payload.cropName || '',
      diseaseName: payload.result || payload.diseaseName || '',
      confidence: Number(payload.confidence) || 0,
      imageUrl: payload.previewUrl || payload.imagePath || '',
      summary: payload.description || ''
    })
    panelStatus.value = 'success'
    ElMessage.success('识别完成')
  } catch {
    panelStatus.value = 'idle'
    ElMessage.error('识别失败，请稍后重试')
  }
}

function goFullResult() {
  if (!resultData.value?.id) return
  router.push(`/user/detect/result/${resultData.value.id}`)
}

function goDiagnosis() {
  if (!resultData.value?.id) return
  router.push(`/user/diagnosis/${resultData.value.id}`)
}
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

/* —— 左：作物 —— */
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
.crop-tag:hover {
  border-color: #86d89a;
  color: #28b446;
}
.crop-tag.active {
  background: #eaf8ee;
  border-color: #28b446;
  color: #1f8f36;
  font-weight: 700;
}
.tag-check {
  color: #28b446;
}

/* —— 中：提示条 —— */
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
  letter-spacing: 0.01em;
}

/* —— 上传区 —— */
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
  min-height: 0;
}
.thumb-list.empty {
  min-height: 0;
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
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
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
.thumb-remove:hover {
  background: #b91c1c;
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
  transition: background 0.15s ease, opacity 0.15s ease, transform 0.15s ease;
}
.btn-detect:hover:not(:disabled) {
  background: #1f8f36;
  transform: translateY(-1px);
}
.btn-detect:disabled {
  opacity: 0.42;
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
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

/* —— 右：动态面板 —— */
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
  opacity: 0.92;
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

.loading-state {
  gap: 16px;
}
.loading-title {
  margin: 0;
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

.result-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 20px;
}
.disease-name {
  margin: 0;
  flex: 1;
  min-width: 0;
  font-size: clamp(1.25rem, 1.6vw, 1.55rem);
  line-height: 1.3;
  color: #14351c;
  font-weight: 800;
  word-break: break-word;
}
.sev-badge {
  flex-shrink: 0;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 0.78rem;
  font-weight: 700;
  line-height: 1.4;
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

.conf-block {
  margin-bottom: 18px;
}
.conf-top {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 10px;
  font-size: 0.88rem;
  color: #64748b;
}
.conf-top strong {
  color: #28b446;
  font-size: 1.15rem;
  font-weight: 800;
}

.advice {
  flex: 1;
  background: #f3f4f6;
  border-radius: 12px;
  padding: 14px 16px;
  margin-bottom: 16px;
  min-height: 0;
}
.advice-title {
  margin: 0 0 8px;
  font-size: 0.85rem;
  font-weight: 700;
  color: #374151;
}
.advice-text {
  margin: 0;
  font-size: 0.88rem;
  line-height: 1.7;
  color: #6b7280;
  display: -webkit-box;
  -webkit-line-clamp: 8;
  -webkit-box-orient: vertical;
  overflow: hidden;
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
