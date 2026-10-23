<template>
  <div class="page" v-loading="loading">
    <template v-if="news">
      <h2>{{ news.title }}</h2>
      <div class="meta">{{ news.createTime }}</div>
      <p class="content">{{ news.content }}</p>
    </template>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { newsDetail } from '@/api/content'
const route = useRoute()
const news = ref(null)
const loading = ref(false)
onMounted(async () => {
  loading.value = true
  try {
    const res = await newsDetail(route.params.id)
    news.value = res.data
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
h2 { color: #1b4332; }
.meta { color: #8aa396; font-size: 0.85rem; margin-bottom: 1rem; }
.content { white-space: pre-wrap; line-height: 1.7; }
</style>
