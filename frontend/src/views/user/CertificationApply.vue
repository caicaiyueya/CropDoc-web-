<template>
  <div class="page">
    <header class="page-head">
      <h2>提交农技员认证</h2>
      <p>普通农户可申请升级为认证农技员，审核通过后可发布农技指导、查看专业病害资料。</p>
    </header>

    <el-alert
      v-if="statusInfo?.status === 'PENDING'"
      title="您已有待审核申请，请耐心等待管理员处理"
      type="warning"
      show-icon
      :closable="false"
      class="mb"
    />
    <el-alert
      v-else-if="statusInfo?.status === 'APPROVED' || store.isExpert"
      title="您已是认证农技员，无需再次提交"
      type="success"
      show-icon
      :closable="false"
      class="mb"
    />
    <el-alert
      v-else-if="statusInfo?.status === 'REJECTED'"
      :title="`上次申请已驳回：${statusInfo?.latest?.reviewComment || '请完善材料后重新提交'}`"
      type="info"
      show-icon
      :closable="false"
      class="mb"
    />

    <el-form v-if="store.isFarmer && statusInfo?.status !== 'PENDING'" label-position="top" @submit.prevent>
      <el-form-item label="资质说明" required>
        <el-input
          v-model="form.qualificationDesc"
          type="textarea"
          :rows="5"
          maxlength="1000"
          show-word-limit
          placeholder="请说明从业经验、资质证书、服务区域等"
        />
      </el-form-item>
      <el-form-item label="证明材料（可选）">
        <el-upload
          :auto-upload="false"
          :limit="1"
          accept="image/jpeg,image/png,image/gif,image/webp,.pdf"
          :on-change="onFileChange"
          :on-remove="() => (form.materialPath = '')"
        >
          <el-button>选择文件并上传</el-button>
        </el-upload>
        <div v-if="form.materialPath" class="path-tip">已上传：{{ form.materialPath }}</div>
      </el-form-item>
      <el-button type="primary" :loading="submitting" class="full" @click="onSubmit">提交申请</el-button>
    </el-form>

    <section class="history">
      <h3>我的申请记录</h3>
      <el-empty v-if="!records.length" description="暂无申请记录" />
      <div v-for="item in records" :key="item.id" class="record">
        <div class="row">
          <el-tag :type="statusType(item.status)" size="small">{{ item.status }}</el-tag>
          <span class="time">{{ item.createTime }}</span>
        </div>
        <p>{{ item.qualificationDesc }}</p>
        <p v-if="item.reviewComment" class="muted">审核意见：{{ item.reviewComment }}</p>
      </div>
    </section>
  </div>
</template>

<script setup>
/**
 * 认证申请页（阶段9完整实现）。
 * 用例：submitCertification / 查询 my、status。
 */
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { myCertifications, certificationStatus, submitCertification } from '@/api/certification'
import { uploadFile } from '@/api/detection'
import { useUserStore } from '@/store/user'

const store = useUserStore()
const form = reactive({ qualificationDesc: '', materialPath: '' })
const submitting = ref(false)
const records = ref([])
const statusInfo = ref(null)

function statusType(s) {
  return { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }[s] || 'info'
}

async function load() {
  const [st, my] = await Promise.all([certificationStatus(), myCertifications()])
  statusInfo.value = st.data
  records.value = my.data || []
}

async function onFileChange(file) {
  if (!file?.raw) return
  const res = await uploadFile(file.raw)
  form.materialPath = res.data.path
  ElMessage.success('材料上传成功')
}

async function onSubmit() {
  if (!form.qualificationDesc.trim()) {
    ElMessage.warning('请填写资质说明')
    return
  }
  submitting.value = true
  try {
    await submitCertification({
      qualificationDesc: form.qualificationDesc.trim(),
      materialPath: form.materialPath || null
    })
    ElMessage.success('申请已提交，等待审核')
    form.qualificationDesc = ''
    form.materialPath = ''
    await load()
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.page-head h2 { margin: 0 0 0.35rem; color: #1b4332; }
.page-head p { margin: 0 0 1rem; color: #52796f; font-size: 0.92rem; line-height: 1.5; }
.mb { margin-bottom: 1rem; }
.full { width: 100%; }
.path-tip { margin-top: 0.4rem; font-size: 0.8rem; color: #52796f; word-break: break-all; }
.history { margin-top: 1.75rem; }
.history h3 { color: #1b4332; font-size: 1rem; }
.record {
  background: #fff;
  border: 1px solid #e0ebe3;
  border-radius: 12px;
  padding: 0.85rem 1rem;
  margin-bottom: 0.75rem;
}
.row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.4rem; }
.time { font-size: 0.78rem; color: #8aa396; }
.muted { color: #6b7c72; font-size: 0.88rem; }
</style>
