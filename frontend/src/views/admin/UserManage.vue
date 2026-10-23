<template>
  <div>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="关键词" clearable style="width: 180px" />
      <el-select v-model="role" clearable placeholder="角色" style="width: 140px">
        <el-option label="FARMER" value="FARMER" />
        <el-option label="EXPERT" value="EXPERT" />
        <el-option label="ADMIN" value="ADMIN" />
      </el-select>
      <el-select v-model="status" clearable placeholder="状态" style="width: 140px">
        <el-option label="NORMAL" value="NORMAL" />
        <el-option label="DISABLED" value="DISABLED" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
    </div>
    <el-table :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="账号" width="140" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="role" label="角色" width="100" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="操作" min-width="280">
        <template #default="{ row }">
          <el-button link @click="onRole(row)">调角色</el-button>
          <el-button link @click="onReset(row)">重置密码</el-button>
          <el-button v-if="row.status === 'NORMAL'" link type="danger" @click="onDisable(row)">禁用</el-button>
          <el-button v-else link type="success" @click="onEnable(row)">启用</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminUserList, changeUserRole, disableUser, enableUser, resetUserPassword } from '@/api/admin'

const list = ref([])
const keyword = ref('')
const role = ref()
const status = ref()

async function load() {
  const res = await adminUserList({
    keyword: keyword.value || undefined,
    role: role.value || undefined,
    status: status.value || undefined,
    page: 1,
    size: 50
  })
  list.value = res.data?.records || []
}

async function onRole(row) {
  const { value } = await ElMessageBox.prompt('输入角色 FARMER/EXPERT/ADMIN', '调整身份', {
    inputValue: row.role
  }).catch(() => ({ value: null }))
  if (!value) return
  await changeUserRole(row.id, { role: value })
  ElMessage.success('已更新')
  load()
}

async function onReset(row) {
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
.toolbar { display: flex; gap: 0.5rem; flex-wrap: wrap; margin-bottom: 1rem; }
</style>
