<template>
  <div class="result-page" v-loading="loading">
    <div class="result-card" v-if="record">
      <div class="name-block">
        <h1>{{ record.result || '未知病害' }}</h1>
        <div class="tags">
          <span class="tag model">NFNet-F1识别</span>
          <span class="tag" :class="severityClass">{{ severityLabel }}</span>
        </div>
      </div>

      <div class="compare">
        <div class="img-col">
          <p class="label">用户上传图</p>
          <div class="frame">
            <img v-if="userImage" :src="userImage" alt="用户上传" />
            <div v-else class="fallback">暂无预览</div>
          </div>
        </div>
        <div class="img-col">
          <p class="label">标准病害图</p>
          <div class="frame ref">
            <img src="/images/hero-farm.jpg" alt="标准病害参考" />
            <span class="badge">参考样例</span>
          </div>
        </div>
      </div>

      <div class="confidence">
        <p class="label">识别置信度</p>
        <p class="value">{{ confidenceText }}</p>
        <p class="desc">{{ record.description || '建议结合田间症状进一步确认，并可进入智能问诊获取防治方案。' }}</p>
      </div>

      <div class="ops">
        <el-button type="primary" size="large" class="btn-main" @click="goDiagnosis">
          立即智能诊断
        </el-button>
        <el-button size="large" class="btn-ghost" @click="$router.push('/user/community')">
          查看农友相关讨论
        </el-button>
      </div>
    </div>
    <el-empty v-else description="暂无识别结果，请先完成拍照识病" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { detectionHistory } from '@/api/detection'
import { useUserStore } from '@/store/user'
import { LAST_DETECT_KEY } from '@/utils/userScopedCache'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const record = ref(null)
const loading = ref(false)

const confidenceText = computed(() => {
  const c = Number(record.value?.confidence)
  if (Number.isNaN(c)) return '--'
  const pct = c <= 1 ? c * 100 : c
  return `${pct.toFixed(1)}%`
})

const severityLabel = computed(() => {
  const c = Number(record.value?.confidence)
  const v = c <= 1 ? c : c / 100
  if (v >= 0.85) return '重度'
  if (v >= 0.6) return '中度'
  return '轻度'
})

const severityClass = computed(() => {
  const label = severityLabel.value
  if (label === '重度') return 'severe'
  if (label === '中度') return 'medium'
  return 'mild'
})

const userImage = computed(() => {
  if (record.value?.previewUrl) return record.value.previewUrl
  if (record.value?.imagePath) {
    const p = record.value.imagePath
    return p.startsWith('http') || p.startsWith('/') ? p : `/uploads/${p}`
  }
  return ''
})

function goDiagnosis() {
  router.push(`/user/diagnosis/${record.value.id}`)
}

onMounted(async () => {
  const uid = userStore.userInfo?.id
  const cached = sessionStorage.getItem(LAST_DETECT_KEY)
  if (cached) {
    try {
      const data = JSON.parse(cached)
      const sameUser =
        data.userId == null || uid == null || String(data.userId) === String(uid)
      const sameRecord =
        !route.params.id || String(data.id) === String(route.params.id)
      if (sameUser && sameRecord) {
        record.value = data
        return
      }
      if (!sameUser) {
        sessionStorage.removeItem(LAST_DETECT_KEY)
      }
    } catch {
      sessionStorage.removeItem(LAST_DETECT_KEY)
    }
  }
  if (!route.params.id) return
  loading.value = true
  try {
    const res = await detectionHistory({ page: 1, size: 50 })
    record.value = (res.data?.records || []).find((r) => String(r.id) === String(route.params.id)) || null
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.result-page {
  display: flex;
  justify-content: center;
  padding: 8px 0 32px;
}
.result-card {
  width: min(860px, 100%);
  background: #fff;
  border-radius: 20px;
  box-shadow: var(--shadow);
  padding: 36px 40px;
}
.name-block { text-align: center; margin-bottom: 28px; }
.name-block h1 {
  margin: 0 0 14px;
  font-size: clamp(1.8rem, 3vw, 2.4rem);
  color: var(--brand);
  font-weight: 800;
  letter-spacing: 0.02em;
}
.tags { display: flex; justify-content: center; gap: 10px; flex-wrap: wrap; }
.tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 0.82rem;
  font-weight: 700;
}
.tag.model { background: var(--brand-soft); color: var(--brand-dark); }
.tag.severe { background: #fee2e2; color: #dc2626; }
.tag.medium { background: #ffedd5; color: #ea580c; }
.tag.mild { background: #e0f2fe; color: #0284c7; }

.compare {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
  margin-bottom: 28px;
}
.label {
  margin: 0 0 8px;
  color: #6b7280;
  font-size: 0.88rem;
  font-weight: 600;
}
.frame {
  position: relative;
  aspect-ratio: 4 / 3;
  border-radius: 14px;
  overflow: hidden;
  background: #f3f4f6;
  border: 1px solid #eef2f0;
}
.frame img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.fallback {
  height: 100%;
  display: grid;
  place-items: center;
  color: #9ca3af;
}
.badge {
  position: absolute;
  left: 10px;
  bottom: 10px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  font-size: 0.75rem;
  padding: 3px 8px;
  border-radius: 6px;
}

.confidence { text-align: center; margin-bottom: 28px; }
.confidence .value {
  margin: 4px 0 10px;
  font-size: 3rem;
  font-weight: 800;
  color: #163b1f;
  line-height: 1;
}
.confidence .desc {
  margin: 0 auto;
  max-width: 560px;
  color: #6b7280;
  line-height: 1.6;
  font-size: 0.92rem;
}

.ops {
  display: flex;
  gap: 14px;
  justify-content: center;
  flex-wrap: wrap;
}
.btn-main { min-width: 180px; }
.btn-ghost {
  min-width: 180px;
  --el-button-bg-color: #fff;
  --el-button-border-color: var(--brand);
  --el-button-text-color: var(--brand);
  --el-button-hover-bg-color: var(--brand-soft);
  --el-button-hover-border-color: var(--brand);
  --el-button-hover-text-color: var(--brand-dark);
}

@media (max-width: 720px) {
  .result-card { padding: 24px 18px; }
  .compare { grid-template-columns: 1fr; }
}
</style>
