<template>
  <div class="create-page">
    <div class="form-card">
      <header class="card-head">
        <h1>{{ store.isExpert ? '发帖 / 专业回答' : '发布帖子' }}</h1>
        <p>分享种植经验、病害求助或日常交流，优质内容审核后将展示在农友交流区。</p>
      </header>

      <el-form
        ref="formRef"
        class="post-form"
        :model="form"
        :rules="rules"
        label-position="top"
        require-asterisk-position="right"
        @submit.prevent
      >
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="form.title"
            maxlength="200"
            show-word-limit
            placeholder="请输入简洁明确的标题"
          />
        </el-form-item>

        <el-form-item label="帖子类型" prop="category">
          <el-radio-group v-model="form.category" class="type-radios">
            <el-radio
              v-for="c in POST_CATEGORIES"
              :key="c.value"
              :label="c.value"
              border
            >
              {{ c.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="store.isExpert" label="农技指导">
          <el-switch
            v-model="form.asGuide"
            active-text="发布为农技指导（官方标签）"
            inactive-text="普通帖"
          />
        </el-form-item>

        <el-form-item label="正文内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            maxlength="5000"
            show-word-limit
            resize="none"
            placeholder="请描述症状、发病环境或经验做法…"
          />
        </el-form-item>

        <el-form-item label="图片（可选，最多 3 张）">
          <el-upload
            v-model:file-list="fileList"
            class="img-uploader"
            drag
            multiple
            list-type="picture"
            :limit="3"
            accept="image/jpeg,image/png,.jpg,.jpeg,.png"
            :http-request="doUpload"
            :before-upload="beforeUpload"
            :on-success="onUploadSuccess"
            :on-remove="onRemove"
            :on-exceed="onExceed"
          >
            <el-icon class="upload-ico" :size="32"><UploadFilled /></el-icon>
            <div class="el-upload__text">将图片拖到此处，或<em>点击上传</em></div>
            <template #tip>
              <p class="tip">支持 JPG / PNG，单张不超过 5MB，最多 3 张；上传后可在下方预览并删除</p>
            </template>
          </el-upload>
        </el-form-item>

        <div class="actions">
          <el-button @click="$router.push('/user/community')">取消</el-button>
          <el-button type="primary" class="btn-publish" round :loading="loading" @click="onSubmit">
            发布
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { createGuidePost, createPost } from '@/api/post'
import { uploadToImgBB } from '@/utils/uploadImage'
import { POST_CATEGORIES, packPostImages } from '@/utils/postMedia'
import { useUserStore } from '@/store/user'

const MAX_SIZE = 5 * 1024 * 1024

const store = useUserStore()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const fileList = ref([])
/** 已成功上传的公网 URL，与 fileList 同步 */
const imageUrls = ref([])

const form = reactive({
  title: '',
  content: '',
  category: 'experience',
  asGuide: false
})

const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, message: '标题至少 2 个字', trigger: 'blur' }
  ],
  category: [{ required: true, message: '请选择帖子类型', trigger: 'change' }],
  content: [
    { required: true, message: '请填写正文', trigger: 'blur' },
    { min: 10, message: '正文至少 10 个字', trigger: 'blur' }
  ]
}

function beforeUpload(file) {
  const ok =
    /^image\/(jpeg|png)$/i.test(file.type) ||
    /\.(jpe?g|png)$/i.test(file.name || '')
  if (!ok) {
    ElMessage.error('仅支持 JPG / PNG 图片')
    return false
  }
  if (file.size > MAX_SIZE) {
    ElMessage.error('单张图片不能超过 5MB')
    return false
  }
  return true
}

function onExceed() {
  ElMessage.warning('最多上传 3 张图片')
}

/**
 * 调用图床上传，返回 https 链接写入数组
 */
async function doUpload({ file, onSuccess, onError }) {
  try {
    const url = await uploadToImgBB(file)
    if (!/^https?:\/\//i.test(url)) {
      throw new Error('图床返回地址无效')
    }
    imageUrls.value = [...imageUrls.value, url].slice(0, 3)
    onSuccess({ url })
    ElMessage.success('图片上传成功')
  } catch (e) {
    onError(e)
    ElMessage.error(e?.message || '图片上传失败')
  }
}

/** 缩略图使用图床 https 地址 */
function onUploadSuccess(res, file) {
  if (res?.url) {
    file.url = res.url
  }
}

function onRemove(file) {
  const url = file?.response?.url || file?.url
  if (url) {
    imageUrls.value = imageUrls.value.filter((u) => u !== url)
  }
  if (file?.url?.startsWith?.('blob:')) {
    try {
      URL.revokeObjectURL(file.url)
    } catch {
      /* ignore */
    }
  }
}

async function onSubmit() {
  if (!formRef.value) return
  const ok = await formRef.value.validate().catch(() => false)
  if (!ok) return

  loading.value = true
  try {
    const images = packPostImages(form.category, imageUrls.value)
    const payload = {
      title: form.title.trim(),
      content: form.content.trim(),
      images
    }
    if (form.asGuide && store.isExpert) {
      await createGuidePost(payload)
    } else {
      await createPost({ ...payload, tag: 'NORMAL' })
    }
    ElMessage.success('已提交，等待审核')
    router.push('/user/community')
  } catch {
    /* 拦截器已提示 */
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.create-page {
  padding: 8px 12px 48px;
  background: transparent;
}

.form-card {
  max-width: 800px;
  margin: 40px auto;
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 8px 28px rgba(15, 23, 42, 0.06);
  padding: 32px 36px 28px;
  border: none;
}

.card-head {
  margin-bottom: 22px;
}
.card-head h1 {
  margin: 0 0 8px;
  font-size: 1.4rem;
  color: #163b1f;
  font-weight: 800;
}
.card-head p {
  margin: 0;
  color: #6b7280;
  font-size: 0.9rem;
  line-height: 1.6;
}

.post-form :deep(.el-form-item__label) {
  font-weight: 650;
  color: #374151;
  padding-bottom: 6px;
}

.post-form :deep(.el-input__wrapper),
.post-form :deep(.el-textarea__inner) {
  border-radius: 12px;
  box-shadow: 0 0 0 1px #eef0f3 inset !important;
  background: #fafbfc;
}
.post-form :deep(.el-input__wrapper:hover),
.post-form :deep(.el-textarea__inner:hover) {
  box-shadow: 0 0 0 1px #c9e8d1 inset !important;
}
.post-form :deep(.el-input__wrapper.is-focus),
.post-form :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px #28b446 inset !important;
  background: #fff;
}

.type-radios {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.type-radios :deep(.el-radio) {
  margin-right: 0;
  border-radius: 12px;
  height: auto;
  padding: 10px 14px;
  border-color: #eef0f3 !important;
  background: #fafbfc;
}
.type-radios :deep(.el-radio.is-checked) {
  border-color: #28b446 !important;
  background: #eaf8ee;
}
.type-radios :deep(.el-radio__input.is-checked .el-radio__inner) {
  background: #28b446;
  border-color: #28b446;
}
.type-radios :deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #1f8f36;
  font-weight: 650;
}

.img-uploader {
  width: 100%;
}
.img-uploader :deep(.el-upload) {
  width: 100%;
}
.img-uploader :deep(.el-upload-dragger) {
  width: 100%;
  padding: 28px 16px;
  border-radius: 14px;
  border: 1.5px dashed #c9e8d1;
  background: #f8fcf9;
  box-shadow: none;
}
.img-uploader :deep(.el-upload-dragger:hover) {
  border-color: #28b446;
  background: #f0faf3;
}
.img-uploader :deep(.el-upload-list--picture .el-upload-list__item) {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.05);
  background: #fafbfc;
}
.upload-ico {
  color: #86d89a;
  margin-bottom: 8px;
}
.img-uploader :deep(.el-upload__text) {
  color: #4b5563;
  font-size: 0.9rem;
}
.img-uploader :deep(.el-upload__text em) {
  color: #28b446;
  font-style: normal;
  font-weight: 650;
}
.tip {
  margin: 8px 0 0;
  font-size: 0.78rem;
  color: #9ca3af;
}

.actions {
  text-align: right;
  margin-top: 8px;
  padding-top: 8px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.btn-publish {
  min-width: 112px;
  font-weight: 700;
  --el-button-bg-color: #28b446;
  --el-button-border-color: #28b446;
  --el-button-hover-bg-color: #1f8f36;
  --el-button-hover-border-color: #1f8f36;
}

@media (max-width: 640px) {
  .form-card {
    margin: 16px auto;
    padding: 22px 16px 20px;
  }
  .actions {
    flex-direction: column-reverse;
  }
  .actions .el-button {
    width: 100%;
  }
}
</style>
