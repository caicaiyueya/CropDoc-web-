<template>
  <div>
    <el-row :gutter="16">
      <el-col :xs="12" :sm="8" :md="6" v-for="item in cards" :key="item.key">
        <el-card shadow="hover" class="stat">
          <div class="num">{{ stats[item.key] ?? '-' }}</div>
          <div class="label">{{ item.label }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { dashboardStats } from '@/api/admin'

const stats = reactive({})
const cards = [
  { key: 'userTotal', label: '用户总数' },
  { key: 'farmerCount', label: '农户' },
  { key: 'expertCount', label: '农技员' },
  { key: 'adminCount', label: '管理员' },
  { key: 'certPending', label: '待审认证' },
  { key: 'postPending', label: '待审帖子' },
  { key: 'todayDetection', label: '今日识别' },
  { key: 'feedbackOpen', label: '待回复反馈' }
]

onMounted(async () => {
  const res = await dashboardStats()
  Object.assign(stats, res.data || {})
})
</script>

<style scoped>
.stat { margin-bottom: 1rem; text-align: center; }
.num { font-size: 1.8rem; font-weight: 700; color: #1b4332; }
.label { color: #6b7c72; margin-top: 0.25rem; }
</style>
