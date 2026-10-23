<template>
  <div>
    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="tag" label="标签" width="100" />
      <el-table-column prop="authorId" label="作者" width="90" />
      <el-table-column prop="createTime" label="时间" width="170" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button link type="success" @click="act(row.id, 'approve')">通过</el-button>
          <el-button link type="danger" @click="act(row.id, 'reject')">驳回</el-button>
          <el-button link @click="act(row.id, 'offline')">下架</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { approvePost, offlinePost, pendingPosts, rejectPost } from '@/api/admin'

const list = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await pendingPosts({ page: 1, size: 50 })
    list.value = res.data?.records || []
  } finally {
    loading.value = false
  }
}

async function act(id, type) {
  if (type === 'approve') await approvePost(id)
  if (type === 'reject') await rejectPost(id)
  if (type === 'offline') await offlinePost(id)
  ElMessage.success('操作成功')
  load()
}

onMounted(load)
</script>
