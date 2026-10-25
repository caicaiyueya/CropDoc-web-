<template>
  <div class="admin-page">
    <el-tabs v-model="tab" class="page-tabs">
      <el-tab-pane label="资讯管理" name="news" />
      <el-tab-pane label="Banner / 公告" name="banner" />
    </el-tabs>

    <!-- 资讯 -->
    <div v-show="tab === 'news'" class="table-container">
      <div class="filter-bar">
        <el-input v-model="newsKw" clearable placeholder="搜索资讯标题" class="filter-input" />
        <el-button type="primary" @click="loadNews">查询</el-button>
        <el-button @click="newsKw = ''">重置</el-button>
        <div class="filter-spacer" />
        <el-button type="primary" @click="openCreateNews">新建资讯</el-button>
      </div>
      <el-table :data="newsFiltered" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="success" link @click="online(row.id)">上架</el-button>
            <el-button type="primary" link @click="offline(row.id)">下架</el-button>
            <el-popconfirm title="确定删除该资讯？" @confirm="removeNews(row.id)">
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Banner / 公告 -->
    <div v-show="tab === 'banner'" class="table-container">
      <h4 class="section-title">Banner</h4>
      <div class="filter-bar">
        <div class="filter-spacer" />
        <el-button type="primary" @click="addBanner">新增 Banner</el-button>
      </div>
      <el-table :data="banners" stripe class="mb">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-popconfirm title="确定删除该 Banner？" @confirm="removeBanner(row.id)">
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <h4 class="section-title">公告</h4>
      <div class="filter-bar">
        <div class="filter-spacer" />
        <el-button type="primary" @click="addNotice">发布公告</el-button>
      </div>
      <el-table :data="notices" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="140" />
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminBannerList,
  adminNewsList,
  adminNoticeList,
  deleteBanner,
  deleteNews,
  newsOffline,
  newsOnline,
  saveBanner,
  saveNews,
  saveNotice
} from '@/api/admin'

const tab = ref('news')
const newsList = ref([])
const newsKw = ref('')
const banners = ref([])
const notices = ref([])

const newsFiltered = computed(() => {
  const q = newsKw.value.trim()
  if (!q) return newsList.value
  return newsList.value.filter((r) => String(r.title || '').includes(q))
})

async function loadNews() {
  const { ensureDemoSynced } = await import('@/utils/adminDemoSeed')
  let res = await adminNewsList({ page: 1, size: 50 })
  let records = res.data?.records || []
  if (!records.length) {
    await ensureDemoSynced()
    res = await adminNewsList({ page: 1, size: 50 })
    records = res.data?.records || []
  }
  newsList.value = records
}

async function loadBanner() {
  const [b, n] = await Promise.all([adminBannerList(), adminNoticeList()])
  banners.value = b.data || []
  notices.value = n.data || []
}

async function openCreateNews() {
  const { value: title } = await ElMessageBox.prompt('标题', '新建资讯').catch(() => ({ value: null }))
  if (!title) return
  const { value: content } = await ElMessageBox.prompt('内容', '新建资讯').catch(() => ({ value: null }))
  if (!content) return
  await saveNews({ title, content, status: 'ONLINE' })
  ElMessage.success('已创建')
  loadNews()
}

async function online(id) {
  await newsOnline(id)
  loadNews()
}
async function offline(id) {
  await newsOffline(id)
  loadNews()
}
async function removeNews(id) {
  await deleteNews(id)
  ElMessage.success('已删除')
  loadNews()
}

async function addBanner() {
  const { value: title } = await ElMessageBox.prompt('标题', 'Banner').catch(() => ({ value: null }))
  if (!title) return
  await saveBanner({ title, imagePath: '/uploads/banner.png', status: 'ONLINE', sortOrder: 1 })
  ElMessage.success('已保存')
  loadBanner()
}

async function removeBanner(id) {
  await deleteBanner(id)
  ElMessage.success('已删除')
  loadBanner()
}

async function addNotice() {
  const { value: title } = await ElMessageBox.prompt('标题', '公告').catch(() => ({ value: null }))
  if (!title) return
  const { value: content } = await ElMessageBox.prompt('内容', '公告').catch(() => ({ value: null }))
  if (!content) return
  await saveNotice({ title, content, status: 'ONLINE' })
  ElMessage.success('已发布')
  loadBanner()
}

onMounted(() => {
  loadNews()
  loadBanner()
})
</script>

<style scoped>
.admin-page {
  max-width: 1200px;
}
.page-tabs :deep(.el-tabs__header) {
  margin-bottom: 12px;
}
.table-container {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(15, 23, 42, 0.04);
  padding: 20px 20px 12px;
}
.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
  align-items: center;
}
.filter-input {
  width: 220px;
}
.filter-spacer {
  flex: 1;
}
.section-title {
  margin: 8px 0 12px;
  color: #163b1f;
  font-size: 0.95rem;
}
.mb {
  margin-bottom: 24px;
}
</style>
