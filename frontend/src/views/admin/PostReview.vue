<template>
  <div class="admin-page">
    <div class="table-container">
      <div class="filter-bar">
        <el-input
          v-model="keyword"
          clearable
          placeholder="搜索帖子标题"
          class="filter-input"
          @keyup.enter="load"
        />
        <el-select v-model="status" clearable placeholder="全部状态" class="filter-select" @change="load">
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已驳回" value="REJECTED" />
          <el-option label="已下架" value="OFFLINE" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="onReset">重置</el-button>
      </div>

      <el-table v-if="filtered.length" :data="filtered" v-loading="loading" stripe class="data-table">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="tag" label="标签" width="100" />
        <el-table-column label="作者" min-width="110">
          <template #default="{ row }">
            {{ row.authorName || `用户#${row.authorId || '—'}` }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagType(row.status)" effect="light">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              type="success"
              link
              @click="act(row.id, 'approve')"
            >
              通过
            </el-button>
            <el-popconfirm
              v-if="row.status === 'PENDING'"
              title="确定驳回该帖子？"
              confirm-button-text="驳回"
              cancel-button-text="取消"
              @confirm="act(row.id, 'reject')"
            >
              <template #reference>
                <el-button type="danger" link>驳回</el-button>
              </template>
            </el-popconfirm>
            <el-popconfirm
              v-if="row.status === 'APPROVED'"
              title="确定下架该帖子？"
              confirm-button-text="下架"
              cancel-button-text="取消"
              @confirm="act(row.id, 'offline')"
            >
              <template #reference>
                <el-button type="primary" link>下架</el-button>
              </template>
            </el-popconfirm>
            <span v-if="row.status === 'REJECTED' || row.status === 'OFFLINE'" class="muted">—</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else-if="!loading" description="暂无帖子" :image-size="100" />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminPostList, approvePost, offlinePost, rejectPost } from '@/api/admin'
import { ensureDemoSynced } from '@/utils/adminDemoSeed'

const list = ref([])
const loading = ref(false)
const keyword = ref('')
const status = ref('')

const filtered = computed(() => {
  const q = keyword.value.trim()
  if (!q) return list.value
  return list.value.filter((r) => String(r.title || '').includes(q))
})

function statusLabel(s) {
  return (
    {
      PENDING: '待审核',
      APPROVED: '已通过',
      REJECTED: '已驳回',
      OFFLINE: '已下架'
    }[s] || s || '—'
  )
}

function statusTagType(s) {
  return (
    {
      PENDING: 'warning',
      APPROVED: 'success',
      REJECTED: 'danger',
      OFFLINE: 'info'
    }[s] || 'info'
  )
}

async function load() {
  loading.value = true
  try {
    const params = { page: 1, size: 100 }
    if (status.value) params.status = status.value
    let res = await adminPostList(params)
    let records = res.data?.records || []
    if (!records.length && !status.value) {
      await ensureDemoSynced()
      res = await adminPostList({ page: 1, size: 100 })
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
  status.value = ''
  load()
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
.filter-select {
  width: 140px;
}
.muted {
  color: #94a3b8;
  font-size: 13px;
}
</style>
