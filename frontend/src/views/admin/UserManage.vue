<template>
  <div class="admin-page">
    <el-tabs v-model="activeTab" class="page-tabs">
      <el-tab-pane label="用户列表" name="users" />
      <el-tab-pane label="认证审核" name="cert" />
    </el-tabs>

    <div v-show="activeTab === 'users'" class="table-container">
      <div class="filter-bar">
        <el-input
          v-model="keyword"
          clearable
          placeholder="搜索账号 / 昵称"
          class="filter-input"
          @keyup.enter="load"
        />
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="onResetFilter">重置</el-button>
      </div>

      <el-table v-if="list.length" :data="list" v-loading="loading" stripe class="data-table">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="账号" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="110" />
        <el-table-column label="是否农技员" width="120">
          <template #default="{ row }">
            <el-tag
              size="small"
              effect="light"
              :type="row.role === 'EXPERT' ? 'success' : 'info'"
            >
              {{ roleLabel(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="region" label="地区" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.region || '—' }}</template>
        </el-table-column>
        <el-table-column prop="contact" label="联系方式" min-width="130" show-overflow-tooltip>
          <template #default="{ row }">{{ row.contact || '—' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 'NORMAL' ? 'success' : 'danger'" effect="plain">
              {{ row.status === 'NORMAL' ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="260" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.role === 'FARMER'"
              type="primary"
              link
              @click="onSetExpert(row)"
            >
              设为农技员
            </el-button>
            <el-button type="primary" link @click="onRole(row)">调角色</el-button>
            <el-button type="primary" link @click="onResetPwd(row)">重置密码</el-button>
            <el-popconfirm
              v-if="row.status === 'NORMAL'"
              title="确定禁用该用户？"
              confirm-button-text="禁用"
              cancel-button-text="取消"
              @confirm="onDisable(row)"
            >
              <template #reference>
                <el-button type="danger" link>禁用</el-button>
              </template>
            </el-popconfirm>
            <el-button v-else type="success" link @click="onEnable(row)">启用</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else-if="!loading" description="暂无农户 / 农技员数据" :image-size="100" />
    </div>

    <div v-show="activeTab === 'cert'" class="cert-wrap">
      <CertificationReview embedded />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminUserList,
  changeUserRole,
  disableUser,
  enableUser,
  resetUserPassword
} from '@/api/admin'
import CertificationReview from './CertificationReview.vue'

const route = useRoute()
const router = useRouter()

const activeTab = ref(route.query.tab === 'cert' ? 'cert' : 'users')
const list = ref([])
const loading = ref(false)
const keyword = ref('')

function roleLabel(r) {
  return { FARMER: '农户', EXPERT: '农技员' }[r] || r || '—'
}

watch(activeTab, (tab) => {
  router.replace({ query: tab === 'cert' ? { tab: 'cert' } : {} })
})

async function load() {
  loading.value = true
  try {
    const res = await adminUserList({
      keyword: keyword.value || undefined,
      page: 1,
      size: 100
    })
    // 仅展示农户与农技员，过滤 ADMIN
    list.value = (res.data?.records || []).filter(
      (u) => u.role === 'FARMER' || u.role === 'EXPERT'
    )
  } finally {
    loading.value = false
  }
}

function onResetFilter() {
  keyword.value = ''
  load()
}

async function onSetExpert(row) {
  try {
    await ElMessageBox.confirm(`将「${row.nickname || row.username}」设为认证农技员？`, '设为农技员', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await changeUserRole(row.id, { role: 'EXPERT' })
  ElMessage.success('已设为农技员')
  load()
}

async function onRole(row) {
  const { value } = await ElMessageBox.prompt('输入角色 FARMER / EXPERT', '调整身份', {
    inputValue: row.role
  }).catch(() => ({ value: null }))
  if (!value || value === 'ADMIN') {
    if (value === 'ADMIN') ElMessage.warning('请勿在此将用户设为管理员')
    return
  }
  await changeUserRole(row.id, { role: value })
  ElMessage.success('已更新')
  load()
}

async function onResetPwd(row) {
  await resetUserPassword(row.id, { newPassword: 'Reset@123' })
  ElMessage.success('密码已重置为 Reset@123')
}

async function onDisable(row) {
  await disableUser(row.id)
  ElMessage.success('已禁用')
  load()
}

async function onEnable(row) {
  await enableUser(row.id)
  ElMessage.success('已启用')
  load()
}

onMounted(load)
</script>

<style scoped>
.admin-page {
  max-width: 1400px;
  margin: 0 auto;
}
.page-tabs :deep(.el-tabs__header) {
  margin-bottom: 12px;
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
  justify-content: flex-start;
}
.filter-input {
  width: 260px;
}
.data-table {
  width: 100%;
}
.cert-wrap {
  width: 100%;
}
.cert-wrap :deep(.admin-page.embedded) {
  max-width: none;
  width: 100%;
}
.cert-wrap :deep(.table-container) {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  padding: 20px 20px 12px;
}
</style>
