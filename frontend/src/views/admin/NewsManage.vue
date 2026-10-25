<template>
  <div class="admin-page">
    <div class="table-container">
      <div class="filter-bar">
        <el-input
          v-model="keyword"
          clearable
          placeholder="搜索资讯标题"
          class="filter-input"
          @keyup.enter="load"
        />
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="onReset">重置</el-button>
        <div class="filter-spacer" />
        <el-button type="primary" @click="openCreate">新建资讯</el-button>
      </div>

      <el-table v-if="filtered.length" :data="filtered" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 'ONLINE' ? 'success' : 'info'" effect="light">
              {{ row.status === 'ONLINE' ? '已上架' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="success" link @click="online(row.id)">上架</el-button>
            <el-button type="primary" link @click="offline(row.id)">下架</el-button>
            <el-popconfirm title="确定删除该资讯？" @confirm="remove(row.id)">
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else-if="!loading" description="暂无资讯，点击右上角新建" :image-size="100" />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminNewsList, deleteNews, newsOffline, newsOnline, saveNews } from '@/api/admin'
import { ensureDemoSynced } from '@/utils/adminDemoSeed'

const list = ref([])
const loading = ref(false)
const keyword = ref('')

const filtered = computed(() => {
  const q = keyword.value.trim()
  if (!q) return list.value
  return list.value.filter((r) => String(r.title || '').includes(q))
})

async function load() {
  loading.value = true
  try {
    let res = await adminNewsList({ page: 1, size: 50 })
    let records = res.data?.records || []
    if (!records.length) {
      await ensureDemoSynced()
      res = await adminNewsList({ page: 1, size: 50 })
      records = res.data?.records || []
    }
    list.value = records
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

function onReset() {
  keyword.value = ''
  load()
}

async function openCreate() {
  const { value: title } = await ElMessageBox.prompt('标题', '新建资讯').catch(() => ({ value: null }))
  if (!title) return
  const { value: content } = await ElMessageBox.prompt('内容', '新建资讯').catch(() => ({ value: null }))
  if (!content) return
  await saveNews({ title, content, status: 'ONLINE' })
  ElMessage.success('已创建，用户端刷新即可看到')
  load()
}

async function online(id) {
  await newsOnline(id)
  ElMessage.success('已上架')
  load()
}

async function offline(id) {
  await newsOffline(id)
  ElMessage.success('已下架')
  load()
}

async function remove(id) {
  await deleteNews(id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.admin-page {
  max-width: 1400px;
  margin: 0 auto;
}
.table-container {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
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
</style>
