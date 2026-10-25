<template>
  <div class="admin-page">
    <div class="table-container">
      <div class="filter-bar">
        <el-select v-model="status" clearable placeholder="状态筛选" class="filter-select">
          <el-option label="待处理" value="OPEN" />
          <el-option label="已回复" value="REPLIED" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="onReset">重置</el-button>
      </div>

      <el-table :data="filtered" v-loading="loading" stripe class="data-table">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="userId" label="用户" width="90" />
        <el-table-column prop="content" label="反馈内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 'REPLIED' ? 'success' : 'warning'" effect="light">
              {{ row.status === 'REPLIED' ? '已回复' : '待处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reply" label="回复" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openReply(row)">
              {{ row.status === 'REPLIED' ? '查看/改回' : '处理' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" title="反馈处理" width="520px" destroy-on-close>
      <div v-if="current" class="reply-dialog">
        <p class="label">用户反馈</p>
        <div class="user-content">{{ current.content }}</div>
        <p class="label">回复内容</p>
        <el-input
          v-model="replyText"
          type="textarea"
          :rows="5"
          maxlength="500"
          show-word-limit
          placeholder="请输入对用户的回复…"
        />
      </div>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitReply">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminFeedbackList, replyFeedback } from '@/api/admin'

const route = useRoute()
const list = ref([])
const loading = ref(false)
const saving = ref(false)
const status = ref()
const visible = ref(false)
const current = ref(null)
const replyText = ref('')

const filtered = computed(() => {
  if (!status.value) return list.value
  return list.value.filter((r) => r.status === status.value)
})

async function load() {
  loading.value = true
  try {
    const res = await adminFeedbackList({ page: 1, size: 50 })
    list.value = res.data?.records || []
    const qid = route.query.id
    if (qid) {
      const hit = list.value.find((r) => String(r.id) === String(qid))
      if (hit) openReply(hit)
    }
  } finally {
    loading.value = false
  }
}

function onReset() {
  status.value = undefined
  load()
}

function openReply(row) {
  current.value = row
  replyText.value = row.reply || ''
  visible.value = true
}

async function submitReply() {
  const text = replyText.value.trim()
  if (!text) {
    ElMessage.warning('请填写回复内容')
    return
  }
  saving.value = true
  try {
    await replyFeedback(current.value.id, { reply: text })
    ElMessage.success('已回复')
    visible.value = false
    await load()
  } finally {
    saving.value = false
  }
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
.filter-select {
  width: 160px;
}
.reply-dialog .label {
  margin: 0 0 8px;
  color: #6b7280;
  font-size: 0.85rem;
}
.user-content {
  margin-bottom: 16px;
  padding: 12px 14px;
  background: #f8faf9;
  border-radius: 10px;
  color: #374151;
  line-height: 1.6;
  white-space: pre-wrap;
}
</style>
