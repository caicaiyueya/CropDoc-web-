<template>
  <div class="page">
    <h2>我的收藏</h2>
    <div v-for="p in list" :key="p.id" class="item" @click="$router.push(`/user/post/${p.id}`)">
      {{ p.title }}
    </div>
    <el-empty v-if="!list.length" description="暂无收藏" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { favoriteList } from '@/api/content'
const list = ref([])
onMounted(async () => {
  const res = await favoriteList()
  list.value = res.data || []
})
</script>

<style scoped>
h2 { color: #1b4332; }
.item {
  background: #fff;
  border: 1px solid #e0ebe3;
  border-radius: 10px;
  padding: 0.85rem 1rem;
  margin-bottom: 0.5rem;
  cursor: pointer;
}
</style>
