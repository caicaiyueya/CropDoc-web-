<template>
  <div>
    <h3>Banner</h3>
    <el-button class="mb" type="primary" @click="addBanner">新增 Banner</el-button>
    <el-table :data="banners" border stripe class="mb">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button link type="danger" @click="removeBanner(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <h3>公告</h3>
    <el-button class="mb" type="primary" @click="addNotice">发布公告</el-button>
    <el-table :data="notices" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100" />
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminBannerList, adminNoticeList, deleteBanner, saveBanner, saveNotice } from '@/api/admin'

const banners = ref([])
const notices = ref([])

async function load() {
  const [b, n] = await Promise.all([adminBannerList(), adminNoticeList()])
  banners.value = b.data || []
  notices.value = n.data || []
}

async function addBanner() {
  const { value: title } = await ElMessageBox.prompt('标题', 'Banner').catch(() => ({ value: null }))
  if (!title) return
  await saveBanner({ title, imagePath: '/uploads/banner.png', status: 'ONLINE', sortOrder: 1 })
  ElMessage.success('已保存')
  load()
}

async function removeBanner(id) {
  await deleteBanner(id)
  load()
}

async function addNotice() {
  const { value: title } = await ElMessageBox.prompt('标题', '公告').catch(() => ({ value: null }))
  if (!title) return
  const { value: content } = await ElMessageBox.prompt('内容', '公告').catch(() => ({ value: null }))
  if (!content) return
  await saveNotice({ title, content, status: 'ONLINE' })
  ElMessage.success('已发布')
  load()
}

onMounted(load)
</script>

<style scoped>
h3 { color: #1b4332; }
.mb { margin-bottom: 1rem; }
</style>
