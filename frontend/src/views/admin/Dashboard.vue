<template>
  <div class="admin-page">
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="8" :md="6" v-for="item in cards" :key="item.key">
        <div class="stat-card">
          <div class="num">{{ stats[item.key] ?? '—' }}</div>
          <div class="label">{{ item.label }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 双图：折线 + TOP5 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :lg="14">
        <div class="panel">
          <div class="panel-head">
            <h3>最近 AI 识别调用量</h3>
            <el-radio-group v-model="range" size="small" @change="renderTrend">
              <el-radio-button label="7d">近 7 天</el-radio-button>
              <el-radio-button label="30d">近一个月</el-radio-button>
            </el-radio-group>
          </div>
          <div ref="trendRef" class="chart-el" />
        </div>
      </el-col>
      <el-col :xs="24" :lg="10">
        <div class="panel">
          <div class="panel-head">
            <h3>病害识别 TOP 5</h3>
          </div>
          <div ref="topRef" class="chart-el" />
        </div>
      </el-col>
    </el-row>

    <!-- 底部左右两栏：待办事项 + 最新用户反馈 -->
    <div class="bottom-row">
      <div class="panel panel-list">
        <div class="panel-head">
          <h3>待办事项</h3>
        </div>
        <ul v-if="todos.length" class="todo-list">
          <li
            v-for="t in todos"
            :key="t.key"
            class="todo-item"
            @click="$router.push(t.path)"
          >
            <span class="todo-badge" :class="t.cls">{{ t.count }}</span>
            <div class="todo-text">
              <strong>{{ t.title }}</strong>
              <p>{{ t.desc }}</p>
            </div>
            <span class="todo-arrow">›</span>
          </li>
        </ul>
        <div v-else class="empty-wrap">
          <el-empty description="暂无待办" :image-size="80" />
        </div>
      </div>

      <div class="panel panel-list">
        <div class="panel-head">
          <h3>最新用户反馈</h3>
          <el-button link type="primary" @click="$router.push('/admin/feedback')">
            全部
          </el-button>
        </div>
        <ul v-if="recentFeedback.length" class="fb-list">
          <li v-for="f in recentFeedback" :key="f.id" class="fb-item">
            <div class="fb-main">
              <div class="fb-top">
                <span class="fb-name">{{ feedbackName(f) }}</span>
                <span class="fb-time">{{ formatTime(f.createTime) }}</span>
              </div>
              <p class="fb-content">{{ f.content }}</p>
            </div>
            <el-button type="primary" link @click="goFeedback(f)">处理</el-button>
          </li>
        </ul>
        <div v-else class="empty-wrap">
          <el-empty description="暂无反馈" :image-size="80" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import * as echarts from 'echarts'
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { adminFeedbackList, dashboardStats } from '@/api/admin'

const router = useRouter()
const stats = reactive({})
const range = ref('7d')
const recentFeedback = ref([])
const trendRef = ref(null)
const topRef = ref(null)
let trendChart = null
let topChart = null

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

const todos = computed(() => {
  const items = []
  const cert = Number(stats.certPending) || 0
  const post = Number(stats.postPending) || 0
  const fb = Number(stats.feedbackOpen) || 0
  if (cert > 0) {
    items.push({
      key: 'cert',
      count: cert,
      title: `${cert} 个农技员待审核`,
      desc: '用户与权限 · 认证审核',
      path: '/admin/certification',
      cls: 'warn'
    })
  }
  if (post > 0) {
    items.push({
      key: 'post',
      count: post,
      title: `${post} 个帖子待审核`,
      desc: '社区管理 · 帖子审核',
      path: '/admin/posts',
      cls: 'info'
    })
  }
  if (fb > 0) {
    items.push({
      key: 'fb',
      count: fb,
      title: `${fb} 条反馈待回复`,
      desc: '反馈处理',
      path: '/admin/feedback',
      cls: 'ok'
    })
  }
  return items
})

function feedbackName(f) {
  return f.nickname || f.username || `用户 #${f.userId}`
}

function formatTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 16)
}

function goFeedback(f) {
  router.push({ path: '/admin/feedback', query: { id: f.id } })
}

function renderTrend() {
  if (!trendRef.value) return
  if (!trendChart) trendChart = echarts.init(trendRef.value)
  const series =
    range.value === '30d'
      ? stats.detectionTrend30 || []
      : stats.detectionTrend7 || []
  const labels = series.map((p) => p.label || p.date)
  const data = series.map((p) => Number(p.count) || 0)
  const days = labels.length || 7
  trendChart.setOption({
    color: ['#28b446'],
    grid: { left: 40, right: 16, top: 24, bottom: days > 14 ? 36 : 28 },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: labels,
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisLabel: {
        color: '#9ca3af',
        fontSize: 11,
        interval: days > 14 ? 4 : 0
      }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#f1f5f9' } },
      axisLabel: { color: '#9ca3af' }
    },
    series: [
      {
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(40,180,70,0.28)' },
            { offset: 1, color: 'rgba(40,180,70,0.02)' }
          ])
        },
        data
      }
    ]
  })
}

function renderTop() {
  if (!topRef.value) return
  if (!topChart) topChart = echarts.init(topRef.value)
  const top = Array.isArray(stats.topDiseases) ? stats.topDiseases : []
  const names = top.map((t) => t.name)
  const values = top.map((t) => Number(t.count) || 0)
  topChart.setOption({
    color: ['#28b446'],
    grid: { left: 96, right: 28, top: 16, bottom: 16 },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    xAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#f1f5f9' } },
      axisLabel: { color: '#9ca3af' }
    },
    yAxis: {
      type: 'category',
      data: [...names].reverse(),
      axisLabel: { color: '#4b5563' },
      axisLine: { show: false },
      axisTick: { show: false }
    },
    series: [
      {
        type: 'bar',
        barWidth: 14,
        data: [...values].reverse(),
        itemStyle: {
          borderRadius: [0, 8, 8, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#86d89a' },
            { offset: 1, color: '#28b446' }
          ])
        }
      }
    ]
  })
}

function onResize() {
  trendChart?.resize()
  topChart?.resize()
}

onMounted(async () => {
  // 登录后台后补齐病害库 / 资讯 / 社区演示种子，与用户端同源
  const { ensureDemoSynced } = await import('@/utils/adminDemoSeed')
  await ensureDemoSynced()
  try {
    const [sRes, fRes] = await Promise.all([
      dashboardStats(),
      adminFeedbackList({ page: 1, size: 10 })
    ])
    Object.assign(stats, sRes.data || {})
    recentFeedback.value = (fRes.data?.records || []).slice(0, 5)
  } catch {
    /* keep empty */
  }
  await nextTick()
  renderTrend()
  renderTop()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  trendChart?.dispose()
  topChart?.dispose()
  trendChart = null
  topChart = null
})
</script>

<style scoped>
.admin-page {
  max-width: 1400px;
  margin: 0 auto;
}

.stat-row {
  margin-bottom: 4px;
}

.stat-card,
.panel {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.stat-card {
  padding: 18px 12px;
  text-align: center;
  margin-bottom: 16px;
}

.num {
  font-size: 1.75rem;
  font-weight: 750;
  color: #1b4332;
}

.label {
  color: #6b7c72;
  margin-top: 4px;
  font-size: 0.85rem;
}

.panel {
  padding: 18px 20px 16px;
  margin-bottom: 16px;
  min-height: 300px;
}

.panel-list {
  min-height: 280px;
  display: flex;
  flex-direction: column;
  margin-bottom: 0;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  flex-shrink: 0;
}

.panel-head h3 {
  margin: 0;
  font-size: 1rem;
  color: #163b1f;
  font-weight: 700;
}

.chart-el {
  width: 100%;
  height: 260px;
}

/* 底部 50/50 Flex 两栏 */
.bottom-row {
  display: flex;
  gap: 16px;
  align-items: stretch;
}

.bottom-row > .panel-list {
  flex: 1 1 50%;
  width: 50%;
  min-width: 0;
}

@media (max-width: 768px) {
  .bottom-row {
    flex-direction: column;
  }

  .bottom-row > .panel-list {
    width: 100%;
    flex-basis: auto;
  }
}

.todo-list,
.fb-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
}

.empty-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 180px;
}

.todo-item,
.fb-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 10px;
  background: #f8faf9;
  border: 1px solid #eef2f0;
}

.todo-item {
  cursor: pointer;
}

.todo-item:hover {
  border-color: #86d89a;
}

.todo-badge {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 750;
  color: #fff;
  flex-shrink: 0;
}

.todo-badge.warn {
  background: #f59e0b;
}

.todo-badge.info {
  background: #3b82f6;
}

.todo-badge.ok {
  background: #28b446;
}

.todo-text,
.fb-main {
  flex: 1;
  min-width: 0;
}

.todo-text strong {
  display: block;
  color: #1f2937;
  font-size: 0.92rem;
}

.todo-text p {
  margin: 2px 0 0;
  color: #9ca3af;
  font-size: 0.78rem;
}

.todo-arrow {
  color: #cbd5e1;
  font-size: 1.2rem;
}

.fb-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 4px;
}

.fb-name {
  font-size: 0.88rem;
  font-weight: 600;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.fb-time {
  flex-shrink: 0;
  font-size: 0.75rem;
  color: #9ca3af;
}

.fb-content {
  margin: 0;
  color: #6b7280;
  font-size: 0.86rem;
  line-height: 1.45;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
