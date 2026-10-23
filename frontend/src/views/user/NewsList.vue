<template>
  <div class="page">
    <h2>农业资讯</h2>
    <div v-for="n in list" :key="n.id" class="item" @click="$router.push(`/user/news/${n.id}`)">
      <div class="title">{{ n.title }}</div>
      <div class="meta">{{ n.createTime }}</div>
    </div>
    <el-empty v-if="!list.length" description="暂无资讯" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { newsList } from '@/api/content'
const list = ref([])
onMounted(async () => {
  const res = await newsList({ page: 1, size: 20 })
  list.value = res.data?.records || []
})
</script>

<style scoped>
h2 { color: #1b4332; }
.item { background: #fff; border: 1px solid #e0ebe3; border-radius: 10px; padding: 0.85rem 1rem; margin-bottom: 0.5rem; cursor: pointer; }
.title { font-weight: 600; color: #1b4332; }
.meta { font-size: 0.8rem; color: #8aa396; margin-top: 0.25rem; }
</style>
