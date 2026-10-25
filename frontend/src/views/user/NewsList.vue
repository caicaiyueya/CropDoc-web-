<template>
  <div class="news-page">
    <header class="page-head">
      <h1>今日农业资讯</h1>
      <p>了解田间管理、病害预警与农技服务动态</p>
    </header>

    <div v-if="loading" class="news-list">
      <div v-for="i in 4" :key="i" class="news-card skeleton">
        <el-skeleton animated :rows="2" />
      </div>
    </div>

    <template v-else>
      <div v-if="list.length" class="news-list">
        <article
          v-for="n in list"
          :key="n.id"
          class="news-card"
          @click="goDetail(n)"
        >
          <div class="cover-wrap">
            <img :src="n.cover || fallbackCover" :alt="n.title" />
          </div>
          <div class="body">
            <h3>{{ n.title }}</h3>
            <p class="summary">{{ n.summary || '点击查看全文' }}</p>
            <time>{{ formatTime(n.createTime) }}</time>
          </div>
        </article>
      </div>

      <div v-else class="empty-wrap">
        <el-empty description="暂无最新农业资讯" :image-size="140" />
      </div>
    </template>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { newsList } from '@/api/content'
import { resolveNewsList } from '@/utils/newsMock'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const fallbackCover = '/images/hero-farm.jpg'

function formatTime(t) {
  if (!t) return '—'
  return String(t).replace('T', ' ').slice(0, 16)
}

function goDetail(n) {
  if (!n?.id) return
  router.push(`/user/news/${n.id}`)
}

async function load() {
  loading.value = true
  try {
    const res = await newsList({ page: 1, size: 10, pageSize: 10 })
    const records = res.data?.records || res.data?.list || []
    list.value = resolveNewsList(records)
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.news-page {
  max-width: 900px;
  margin: 0 auto;
  padding-bottom: 40px;
}

.page-head {
  margin-bottom: 22px;
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
  font-size: 0.9rem;
}

.news-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.news-card {
  display: flex;
  gap: 16px;
  padding: 14px;
  background: #fff;
  border: 1px solid #eef2f0;
  border-radius: 16px;
  cursor: pointer;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.04);
  transition: border-color 0.15s ease, box-shadow 0.15s ease, transform 0.15s ease;
}
.news-card:hover {
  border-color: #c5e8cd;
  box-shadow: 0 10px 24px rgba(40, 180, 70, 0.1);
  transform: translateY(-1px);
}
.news-card.skeleton {
  cursor: default;
  padding: 20px;
}
.news-card.skeleton:hover {
  transform: none;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.04);
}

.cover-wrap {
  width: 132px;
  height: 96px;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;
  background: #eef2f0;
}
.cover-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 6px;
}
.body h3 {
  margin: 0;
  font-size: 1.05rem;
  color: #1f2937;
  font-weight: 750;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.summary {
  margin: 0;
  color: #6b7280;
  font-size: 0.86rem;
  line-height: 1.55;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
time {
  font-size: 0.78rem;
  color: #9ca3af;
}

.empty-wrap {
  min-height: 360px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 16px;
  border: 1px solid #eef2f0;
}

@media (max-width: 640px) {
  .news-card {
    flex-direction: column;
  }
  .cover-wrap {
    width: 100%;
    height: 160px;
  }
}
</style>
