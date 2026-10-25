<template>
  <div class="cert-page">
    <div class="cert-shell">
      <!-- 上部：申请表单 -->
      <el-card class="cert-card" shadow="never">
        <header class="card-head">
          <h2>提交农技员认证</h2>
          <p>填写资质说明并上传证明材料，审核通过后可发布农技指导、查看专业病害资料。</p>
        </header>

        <el-alert
          v-if="statusInfo?.status === 'PENDING'"
          class="status-alert"
          type="warning"
          show-icon
          :closable="false"
          title="您已有待审核申请，请耐心等待管理员处理"
        />
        <el-alert
          v-else-if="statusInfo?.status === 'APPROVED' || store.isExpert"
          class="status-alert"
          type="success"
          show-icon
          :closable="false"
          title="您已是认证农技员，无需再次提交"
        />
        <el-alert
          v-else-if="statusInfo?.status === 'REJECTED'"
          class="status-alert"
          type="info"
          show-icon
          :closable="false"
          :title="`上次申请已驳回：${statusInfo?.latest?.reviewComment || '请完善材料后重新提交'}`"
        />

        <el-form
          v-if="canSubmit"
          ref="formRef"
          class="cert-form"
          :model="form"
          :rules="rules"
          label-position="top"
          require-asterisk-position="right"
          @submit.prevent
        >
          <el-form-item label="资质说明" prop="qualificationDesc">
            <el-input
              v-model="form.qualificationDesc"
              type="textarea"
              :rows="4"
              maxlength="1000"
              show-word-limit
              resize="none"
              placeholder="请说明从业经验、资质证书、服务区域等（必填）"
            />
          </el-form-item>

          <el-form-item label="证明材料" prop="fileList">
            <el-upload
              v-model:file-list="fileList"
              class="cert-uploader"
              drag
              multiple
              :limit="5"
              :http-request="mockUpload"
              :before-upload="beforeUpload"
              :on-remove="onRemove"
              :on-exceed="onExceed"
              accept="image/jpeg,image/png,image/gif,image/webp,.pdf"
            >
              <el-icon class="upload-icon" :size="36"><UploadFilled /></el-icon>
              <div class="el-upload__text">
                将文件拖到此处，或<em>点击上传</em>
              </div>
              <template #tip>
                <p class="upload-tip">
                  支持 JPG / PNG / GIF / WebP / PDF，单文件不超过 5MB，最多 5 个
                </p>
              </template>
            </el-upload>
          </el-form-item>

          <div class="form-actions">
            <el-button type="primary" :loading="submitting" @click="onSubmit">
              提交申请
            </el-button>
          </div>
        </el-form>
      </el-card>

      <!-- 下部：申请记录 -->
      <el-card class="cert-card" shadow="never">
        <header class="card-head compact">
          <h2>我的申请记录</h2>
        </header>

        <el-empty
          v-if="!records.length"
          description="暂无申请记录"
          :image-size="96"
        />

        <el-table
          v-else
          :data="records"
          stripe
          class="record-table"
          empty-text="暂无申请记录"
        >
          <el-table-column
            prop="createTime"
            label="申请时间"
            width="180"
            show-overflow-tooltip
          />
          <el-table-column label="资质说明" min-width="240">
            <template #default="{ row }">
              <span class="desc-cell" :title="row.qualificationDesc">
                {{ truncate(row.qualificationDesc, 48) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" size="small" effect="light" round>
                {{ statusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
/**
 * 提交农技员认证（Vue 3 Composition API + Element Plus）
 * 用例：submitCertification / my / status
 */
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import {
  myCertifications,
  certificationStatus,
  submitCertification
} from '@/api/certification'
import { uploadFile } from '@/api/detection'
import { useUserStore } from '@/store/user'

const MAX_SIZE = 5 * 1024 * 1024

const store = useUserStore()
const formRef = ref(null)
const form = reactive({
  qualificationDesc: ''
})
const fileList = ref([])
const submitting = ref(false)
const records = ref([])
const statusInfo = ref(null)

const canSubmit = computed(
  () => store.isFarmer && statusInfo.value?.status !== 'PENDING' && !store.isExpert
)

const rules = {
  qualificationDesc: [
    { required: true, message: '请填写资质说明', trigger: 'blur' },
    { min: 10, message: '请至少填写 10 个字', trigger: 'blur' },
    { max: 1000, message: '最多 1000 字', trigger: 'blur' }
  ]
}

function statusTagType(s) {
  return { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }[s] || 'info'
}

function statusLabel(s) {
  return { PENDING: '审核中', APPROVED: '已通过', REJECTED: '已驳回' }[s] || s || '未知'
}

function truncate(text, n) {
  const t = (text || '').trim()
  if (t.length <= n) return t || '—'
  return `${t.slice(0, n)}…`
}

/** 模拟上传成功（展示文件列表，允许删除） */
function mockUpload({ file, onSuccess, onError }) {
  window.setTimeout(() => {
    try {
      const mockUrl = URL.createObjectURL(file)
      onSuccess({
        name: file.name,
        url: mockUrl,
        mock: true,
        raw: file
      })
      ElMessage.success(`${file.name} 上传成功`)
    } catch (e) {
      onError(e)
      ElMessage.error('上传失败，请重试')
    }
  }, 450)
}

function beforeUpload(file) {
  const okType =
    /^image\/(jpeg|png|gif|webp)$/i.test(file.type) ||
    file.type === 'application/pdf' ||
    /\.pdf$/i.test(file.name)
  if (!okType) {
    ElMessage.error('仅支持图片或 PDF')
    return false
  }
  if (file.size > MAX_SIZE) {
    ElMessage.error('单个文件不能超过 5MB')
    return false
  }
  return true
}

function onRemove(file) {
  if (file?.url?.startsWith?.('blob:')) {
    try {
      URL.revokeObjectURL(file.url)
    } catch {
      /* ignore */
    }
  }
}

function onExceed() {
  ElMessage.warning('最多上传 5 个文件')
}

async function load() {
  const [st, my] = await Promise.all([certificationStatus(), myCertifications()])
  statusInfo.value = st.data
  records.value = my.data || []
}

async function resolveMaterialPath() {
  if (!fileList.value.length) return null
  // 优先取带 raw 的本地文件做一次真实上传，便于后端落库
  const first = fileList.value.find((f) => f.raw) || fileList.value[0]
  const raw = first.raw
  if (raw) {
    const res = await uploadFile(raw)
    return res.data?.path || null
  }
  return first.name ? `/uploads/cert/${first.name}` : null
}

async function onSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const materialPath = await resolveMaterialPath()
    await submitCertification({
      qualificationDesc: form.qualificationDesc.trim(),
      materialPath
    })
    ElMessage.success('申请已提交，等待审核')
    form.qualificationDesc = ''
    fileList.value.forEach((f) => {
      if (f?.url?.startsWith?.('blob:')) {
        try {
          URL.revokeObjectURL(f.url)
        } catch {
          /* ignore */
        }
      }
    })
    fileList.value = []
    formRef.value?.resetFields?.()
    await load()
  } catch {
    /* 错误已由 request 拦截器提示 */
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.cert-page {
  min-height: calc(100vh - 160px);
  background: #f7f8fa;
  border-radius: 16px;
  padding: 28px 20px 40px;
  box-sizing: border-box;
}

.cert-shell {
  max-width: 900px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.cert-card {
  border: 1px solid #eef0f3;
  border-radius: 14px;
  background: #fff;
}

.cert-card :deep(.el-card__body) {
  padding: 28px 28px 24px;
}

.card-head h2 {
  margin: 0 0 8px;
  font-size: 1.35rem;
  font-weight: 750;
  color: #163b1f;
  letter-spacing: 0.01em;
}

.card-head p {
  margin: 0;
  font-size: 0.9rem;
  line-height: 1.65;
  color: #6b7280;
}

.card-head.compact h2 {
  margin-bottom: 0;
  font-size: 1.15rem;
}

.status-alert {
  margin: 18px 0 0;
  border-radius: 10px;
}

.cert-form {
  margin-top: 22px;
}

.cert-form :deep(.el-form-item__label) {
  font-weight: 650;
  color: #374151;
  padding-bottom: 6px;
  line-height: 1.4;
}

.cert-form :deep(.el-textarea__inner) {
  border-radius: 10px;
  padding: 12px 14px;
  font-size: 0.92rem;
  line-height: 1.65;
  box-shadow: 0 0 0 1px #e5e7eb inset;
  border: none;
  transition: box-shadow 0.15s ease;
}

.cert-form :deep(.el-textarea__inner:hover) {
  box-shadow: 0 0 0 1px #c9e8d1 inset;
}

.cert-form :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px #28b446 inset !important;
}

.cert-uploader {
  width: 100%;
}

.cert-uploader :deep(.el-upload) {
  width: 100%;
}

.cert-uploader :deep(.el-upload-dragger) {
  width: 100%;
  padding: 28px 20px;
  border-radius: 12px;
  border: 1.5px dashed #c9e8d1;
  background: #f8fcf9;
  transition: 0.15s ease;
}

.cert-uploader :deep(.el-upload-dragger:hover) {
  border-color: #28b446;
  background: #f0faf3;
}

.upload-icon {
  color: #86d89a;
  margin-bottom: 8px;
}

.cert-uploader :deep(.el-upload__text) {
  font-size: 0.9rem;
  color: #4b5563;
}

.cert-uploader :deep(.el-upload__text em) {
  color: #28b446;
  font-style: normal;
  font-weight: 650;
}

.upload-tip {
  margin: 8px 0 0;
  font-size: 0.78rem;
  color: #9ca3af;
  line-height: 1.5;
}

.cert-uploader :deep(.el-upload-list) {
  margin-top: 10px;
}

.cert-uploader :deep(.el-upload-list__item) {
  border-radius: 8px;
  margin-top: 6px;
  transition: background 0.15s ease;
}

.form-actions {
  text-align: right;
  margin-top: 8px;
  padding-top: 4px;
}

.form-actions :deep(.el-button) {
  min-width: 112px;
  border-radius: 10px;
  font-weight: 650;
  --el-button-bg-color: #28b446;
  --el-button-border-color: #28b446;
  --el-button-hover-bg-color: #1f8f36;
  --el-button-hover-border-color: #1f8f36;
  --el-button-active-bg-color: #1a7a2e;
  --el-button-active-border-color: #1a7a2e;
}

.record-table {
  margin-top: 16px;
  width: 100%;
  --el-table-border-color: #eef0f3;
  --el-table-header-bg-color: #f8faf9;
}

.record-table :deep(.el-table__header th) {
  font-weight: 650;
  color: #374151;
  font-size: 0.86rem;
}

.record-table :deep(.el-table__cell) {
  padding: 12px 0;
}

.desc-cell {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: bottom;
  color: #4b5563;
  font-size: 0.9rem;
}

@media (max-width: 640px) {
  .cert-page {
    margin: 0;
    padding: 16px 12px 32px;
  }

  .cert-card :deep(.el-card__body) {
    padding: 20px 16px 18px;
  }

  .form-actions {
    text-align: stretch;
  }

  .form-actions :deep(.el-button) {
    width: 100%;
  }
}
</style>
