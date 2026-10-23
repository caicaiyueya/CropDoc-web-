<template>
  <div class="page" v-loading="loading">
    <h2>识别结果</h2>
    <template v-if="record">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="病害名称">{{ record.result }}</el-descriptions-item>
        <el-descriptions-item label="置信度">{{ record.confidence }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ record.description }}</el-descriptions-item>
        <el-descriptions-item label="图片路径">{{ record.imagePath }}</el-descriptions-item>
      </el-descriptions>
      <div class="actions">
        <el-button type="primary" @click="$router.push(`/user/diagnosis/${record.id}`)">继续智能诊断</el-button>
        <el-button @click="$router.push('/user/detect')">再次识别</el-button>
      </div>
    </template>
    <el-empty v-else description="暂无结果" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { detectionHistory } from '@/api/detection'

const route = useRoute()
const record = ref(null)
const loading = ref(false)

onMounted(async () => {
  const cached = sessionStorage.getItem('ynzz_last_detect')
  if (cached) {
    const data = JSON.parse(cached)
    if (!route.params.id || String(data.id) === String(route.params.id)) {
      record.value = data
      return
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
h2 { color: #1b4332; }
.actions { margin-top: 1rem; display: flex; gap: 0.75rem; flex-wrap: wrap; }
</style>
