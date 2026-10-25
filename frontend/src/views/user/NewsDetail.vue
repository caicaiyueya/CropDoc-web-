<template>
  <div class="page" v-loading="loading">
    <template v-if="news">
      <div v-if="news.cover" class="cover">
        <img :src="news.cover" :alt="news.title" />
      </div>
      <h1 class="title">{{ news.title }}</h1>
      <div class="meta">{{ formatTime(news.createTime) }}</div>
      <div class="content">{{ news.content }}</div>

      <div class="actions">
        <el-button @click="goBack">返回列表</el-button>
        <el-button type="primary" plain @click="onShare">分享</el-button>
      </div>
    </template>
    <el-empty v-else-if="!loading" description="资讯不存在或已下线" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { newsDetail } from '@/api/content'
import { resolveNewsCoverByTitle } from '@/utils/newsMock'

const route = useRoute()
const router = useRouter()
const news = ref(null)
const loading = ref(false)

function formatTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 16)
}

function goBack() {
  router.push('/user/news')
}

async function onShare() {
  const url = window.location.href
  try {
    if (navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(url)
      ElMessage.success('链接已复制，可分享给好友')
    } else {
      ElMessage.info(url)
    }
  } catch {
    ElMessage.info(url)
  }
}

onMounted(async () => {
  const id = route.params.id
  loading.value = true
  try {
    const res = await newsDetail(id)
    news.value = res.data
      ? {
          ...res.data,
          cover:
            res.data.cover ||
            resolveNewsCoverByTitle(res.data.title) ||
            '/images/hero-farm.jpg'
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
  max-width: 900px;
  margin: 0 auto;
  background: #fff;
  border-radius: 16px;
  padding: 28px 32px 36px;
  border: 1px solid #eef2f0;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.04);
}

.cover {
  width: 100%;
  max-height: 400px;
  height: 360px;
  margin: 0 0 24px;
  border-radius: 12px;
  overflow: hidden;
  background: #f7f8fa;
}

.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.title {
  margin: 0 0 10px;
  color: #163b1f;
  font-size: 1.65rem;
  font-weight: 800;
  line-height: 1.35;
}

.meta {
  color: #9ca3af;
  font-size: 0.85rem;
  margin-bottom: 1.4rem;
}

.content {
  margin: 0 0 28px;
  white-space: pre-wrap;
  line-height: 1.8;
  color: #374151;
  font-size: 1rem;
  letter-spacing: 0.01em;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding-top: 8px;
  border-top: 1px solid #f0f2f1;
}

@media (max-width: 640px) {
  .page {
    padding: 20px 16px 28px;
  }
  .cover {
    height: 220px;
    max-height: 260px;
  }
  .title {
    font-size: 1.35rem;
  }
}
</style>
