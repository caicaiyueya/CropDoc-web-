<template>
  <div class="page">
    <div class="toolbar">
      <el-radio-group v-model="status" @change="load">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="PENDING">待审核</el-radio-button>
        <el-radio-button label="APPROVED">已通过</el-radio-button>
        <el-radio-button label="REJECTED">已驳回</el-radio-button>
      </el-radio-group>
      <el-button @click="loadLogs">审核日志</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="userId" label="用户ID" width="90" />
      <el-table-column prop="qualificationDesc" label="资质说明" min-width="220" show-overflow-tooltip />
      <el-table-column prop="materialPath" label="材料" min-width="140" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="申请时间" width="170" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 'PENDING'">
            <el-button type="success" link @click="onApprove(row)">通过</el-button>
            <el-button type="danger" link @click="onReject(row)">驳回</el-button>
          </template>
          <span v-else class="muted">{{ row.reviewComment || '—' }}</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        background
        layout="prev, pager, next, total"
        :total="total"
        v-model:current-page="page"
        :page-size="size"
        @current-change="load"
      />
    </div>

    <el-dialog v-model="logVisible" title="认证审核日志" width="640px">
      <el-timeline>
        <el-timeline-item v-for="log in logs" :key="log.id" :timestamp="log.createTime">
          {{ log.action }} — {{ log.detail }}
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 管理员认证审核页（阶段9完整实现）。
 * 用例：listCertifications / approveCertification / rejectCertification。
 * 说明：通过后用户同 token 下次请求即以 EXPERT 生效（后端过滤器从 DB 加载 role）。
 */
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminCertList,
  approveCertification,
  rejectCertification,
  certificationLogs
} from '@/api/certification'

const status = ref('PENDING')
const list = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const logVisible = ref(false)
const logs = ref([])

function statusType(s) {
  return { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }[s] || 'info'
}

async function load() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (status.value) params.status = status.value
    const res = await adminCertList(params)
    list.value = res.data?.records || []
    total.value = Number(res.data?.total || 0)
  } finally {
    loading.value = false
  }
}

async function onApprove(row) {
  const { value } = await ElMessageBox.prompt('审核意见（可选）', '通过认证', {
    confirmButtonText: '通过并升级为 EXPERT',
    cancelButtonText: '取消',
    inputPlaceholder: '材料齐全',
    inputValue: '材料齐全，同意升级'
  }).catch(() => ({ value: null }))
  if (value === null) return
  await approveCertification(row.id, { reviewComment: value })
  ElMessage.success('已通过，用户下次请求即为 EXPERT')
  load()
}

async function onReject(row) {
  const { value } = await ElMessageBox.prompt('请填写驳回原因', '驳回认证', {
    confirmButtonText: '确认驳回',
    cancelButtonText: '取消',
    inputPlaceholder: '材料不完整',
    inputValidator: (v) => (!!v && v.trim() ? true : '请填写原因')
  }).catch(() => ({ value: null }))
  if (value === null) return
  await rejectCertification(row.id, { reviewComment: value })
  ElMessage.success('已驳回，用户仍为 FARMER')
  load()
}

async function loadLogs() {
  const res = await certificationLogs()
  logs.value = res.data || []
  logVisible.value = true
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  gap: 1rem;
  flex-wrap: wrap;
}
.pager {
  margin-top: 1rem;
  display: flex;
  justify-content: flex-end;
}
.muted { color: #8aa396; font-size: 0.85rem; }
</style>
