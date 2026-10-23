<template>
  <div class="page">
    <h2>识别历史</h2>
    <el-empty v-if="!list.length" description="暂无记录" />
    <div v-for="item in list" :key="item.id" class="item" @click="$router.push(`/user/detect/result/${item.id}`)">
      <div class="title">{{ item.result || '未命名结果' }}</div>
      <div class="meta">置信度 {{ item.confidence }} · {{ item.createTime }}</div>
    </div>
    <div class="pager">
      <el-pagination layout="prev, pager, next" :total="total" v-model:current-page="page" :page-size="10" @current-change="load" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { detectionHistory } from '@/api/detection'

const list = ref([])
const page = ref(1)
const total = ref(0)

async function load() {
  const res = await detectionHistory({ page: page.value, size: 10 })
  list.value = res.data?.records || []
  total.value = Number(res.data?.total || 0)
}
onMounted(load)
</script>

<style scoped>
h2 { color: #1b4332; }
.item {
  background: #fff;
  border: 1px solid #e0ebe3;
  border-radius: 12px;
  padding: 0.85rem 1rem;
  margin-bottom: 0.6rem;
  cursor: pointer;
}
.title { font-weight: 600; color: #1b4332; }
.meta { font-size: 0.8rem; color: #8aa396; margin-top: 0.25rem; }
.pager { display: flex; justify-content: center; margin-top: 1rem; }
</style>
