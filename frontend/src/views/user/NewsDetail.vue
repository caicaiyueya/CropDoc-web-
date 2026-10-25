<template>
  <div class="page" v-loading="loading">
    <template v-if="news">
      <div v-if="news.cover" class="cover">
        <img :src="news.cover" :alt="news.title" />
      </div>
      <h2>{{ news.title }}</h2>
      <div class="meta">{{ formatTime(news.createTime) }}</div>
      <p class="content">{{ news.content }}</p>
    </template>
    <el-empty v-else-if="!loading" description="资讯不存在或已下线" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { newsDetail } from '@/api/content'
import { MOCK_NEWS } from '@/utils/newsMock'

const route = useRoute()
const news = ref(null)
const loading = ref(false)

function formatTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 16)
}

onMounted(async () => {
  const id = route.params.id
  // Mock 详情：由列表 query 带入，或按 mock id 查找
  if (id === 'mock' || String(id).startsWith('mock-')) {
    if (route.query.title) {
      news.value = {
        title: route.query.title,
        createTime: route.query.time || '',
        content: route.query.content || '',
        cover: '/images/hero-farm.jpg'
      }
    } else {
      news.value = MOCK_NEWS.find((n) => n.id === id) || MOCK_NEWS[0]
    }
    return
  }

  loading.value = true
  try {
    const res = await newsDetail(id)
    news.value = res.data
      ? {
          ...res.data,
          cover: res.data.cover || '/images/hero-farm.jpg'
        }
      : null
  } catch {
    news.value = null
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.page {
  max-width: 800px;
  margin: 0 auto;
  background: #fff;
  border-radius: 16px;
  padding: 24px 28px 36px;
  border: 1px solid #eef2f0;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.04);
}
.cover {
  margin: -8px -12px 18px;
  height: 220px;
  border-radius: 12px;
  overflow: hidden;
  background: #eef2f0;
}
.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
h2 {
  margin: 0 0 8px;
  color: #163b1f;
  font-size: 1.45rem;
  line-height: 1.35;
}
.meta {
  color: #9ca3af;
  font-size: 0.85rem;
  margin-bottom: 1.1rem;
}
.content {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.75;
  color: #374151;
  font-size: 0.98rem;
}
</style>
