<template>
  <div>
    <el-table :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="userId" label="用户" width="90" />
      <el-table-column prop="content" label="内容" min-width="200" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="reply" label="回复" min-width="160" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button link type="primary" @click="onReply(row)">回复</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminFeedbackList, replyFeedback } from '@/api/admin'

const list = ref([])
async function load() {
  const res = await adminFeedbackList({ page: 1, size: 50 })
  list.value = res.data?.records || []
}
async function onReply(row) {
  const { value } = await ElMessageBox.prompt('回复内容', '反馈回复').catch(() => ({ value: null }))
  if (!value) return
  await replyFeedback(row.id, { reply: value })
  ElMessage.success('已回复')
  load()
}
onMounted(load)
</script>
