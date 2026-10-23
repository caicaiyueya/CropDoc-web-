<template>
  <div>
    <el-button type="primary" class="mb" @click="openCreate">新建资讯</el-button>
    <el-table :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button link type="success" @click="online(row.id)">上架</el-button>
          <el-button link @click="offline(row.id)">下架</el-button>
          <el-button link type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminNewsList, deleteNews, newsOffline, newsOnline, saveNews } from '@/api/admin'

const list = ref([])
async function load() {
  const res = await adminNewsList({ page: 1, size: 50 })
  list.value = res.data?.records || []
}
async function openCreate() {
  const { value: title } = await ElMessageBox.prompt('标题', '新建资讯').catch(() => ({ value: null }))
  if (!title) return
  const { value: content } = await ElMessageBox.prompt('内容', '新建资讯').catch(() => ({ value: null }))
  if (!content) return
  await saveNews({ title, content, status: 'ONLINE' })
  ElMessage.success('已创建')
  load()
}
async function online(id) { await newsOnline(id); load() }
async function offline(id) { await newsOffline(id); load() }
async function remove(id) { await deleteNews(id); load() }
onMounted(load)
</script>

<style scoped>
.mb { margin-bottom: 1rem; }
</style>
