<template>
  <div class="admin-page" :class="{ embedded: embedded }">
    <div class="table-container">
      <div class="filter-bar">
        <el-radio-group v-model="status" @change="load">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="PENDING">待审核</el-radio-button>
          <el-radio-button label="APPROVED">已通过</el-radio-button>
          <el-radio-button label="REJECTED">已驳回</el-radio-button>
        </el-radio-group>
        <div class="filter-spacer" />
        <el-button @click="loadLogs">审核日志</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
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
              <el-popconfirm
                title="确定驳回该认证申请？"
                confirm-button-text="去填写原因"
                @confirm="onReject(row)"
              >
                <template #reference>
                  <el-button type="danger" link>驳回</el-button>
                </template>
              </el-popconfirm>
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
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminCertList,
  approveCertification,
  rejectCertification,
  certificationLogs
} from '@/api/certification'

const props = defineProps({
  /** 嵌入用户管理 Tab 时隐藏外层卡片阴影重复 */
  embedded: { type: Boolean, default: false }
})

const embedded = computed(() => props.embedded)

/** 默认看「已通过」，演示农技员会预置在此；可切到待审核处理新申请 */
const status = ref('APPROVED')
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
/* 与用户管理等后台页统一内容宽度，避免侧栏入口与 Tab 内嵌白底不一致 */
.admin-page {
  max-width: 1400px;
  margin: 0 auto;
}
.admin-page.embedded {
  max-width: none;
  width: 100%;
}
.table-container {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  padding: 20px 20px 12px;
  width: 100%;
  box-sizing: border-box;
}
.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
  align-items: center;
}
.filter-spacer {
  flex: 1;
}
.pager {
  margin-top: 1rem;
  display: flex;
  justify-content: flex-end;
}
.muted {
  color: #8aa396;
  font-size: 0.85rem;
}
</style>
