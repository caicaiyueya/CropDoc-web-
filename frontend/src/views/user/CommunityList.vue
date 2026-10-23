<template>
  <div class="page">
    <div class="toolbar">
      <h2>社区</h2>
      <el-button type="primary" @click="$router.push('/user/post-create')">发帖</el-button>
    </div>
    <el-radio-group v-model="sort" size="small" @change="load" class="mb">
      <el-radio-button label="time">最新</el-radio-button>
      <el-radio-button label="hot">热门</el-radio-button>
    </el-radio-group>
    <div v-for="p in list" :key="p.id" class="item" @click="$router.push(`/user/post/${p.id}`)">
      <div class="title">
        <el-tag v-if="p.tag === 'GUIDE'" size="small" type="success">指导</el-tag>
        {{ p.title }}
      </div>
      <div class="meta">浏览 {{ p.viewCount }} · {{ p.createTime }}</div>
    </div>
    <el-empty v-if="!list.length" description="暂无帖子" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { postList } from '@/api/post'

const list = ref([])
const sort = ref('time')

async function load() {
  const res = await postList({ page: 1, size: 20, sort: sort.value })
  list.value = res.data?.records || []
}
onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; }
h2 { margin: 0; color: #1b4332; }
.mb { margin: 0.75rem 0; }
.item {
  background: #fff;
  border: 1px solid #e0ebe3;
  border-radius: 12px;
  padding: 0.85rem 1rem;
  margin-bottom: 0.6rem;
  cursor: pointer;
}
.title { font-weight: 600; color: #1b4332; display: flex; gap: 0.4rem; align-items: center; }
.meta { font-size: 0.8rem; color: #8aa396; margin-top: 0.3rem; }
</style>
