<template>
  <div>
    <el-table :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="reporterId" label="举报人" width="90" />
      <el-table-column prop="targetType" label="类型" width="100" />
      <el-table-column prop="targetId" label="目标ID" width="90" />
      <el-table-column prop="reason" label="原因" min-width="180" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="处理" width="160">
        <template #default="{ row }">
          <el-button link type="warning" @click="onWarn(row)">警告作者</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { reportList, warnUser } from '@/api/admin'

const list = ref([])
onMounted(async () => {
  const res = await reportList({ page: 1, size: 50 })
  list.value = res.data?.records || []
})

async function onWarn(row) {
  const { value } = await ElMessageBox.prompt('警告内容', '警告用户').catch(() => ({ value: null }))
  if (!value) return
  await warnUser(row.reporterId, { reason: value })
  ElMessage.success('已记录警告')
}
</script>
