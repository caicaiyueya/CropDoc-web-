<template>
  <div class="page">
    <header class="page-head">
      <h2>病虫害识别</h2>
      <p>上传作物叶片或受害部位照片，系统将调用视觉模型给出识别结果。</p>
    </header>

    <el-form label-position="top">
      <el-form-item label="选择作物（可选）">
        <el-select v-model="cropId" clearable placeholder="请选择作物" style="width: 100%">
          <el-option v-for="c in crops" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>

      <el-form-item label="上传图片" required>
        <el-upload
          class="uploader"
          drag
          :auto-upload="false"
          :show-file-list="false"
          accept="image/jpeg,image/png,image/gif,image/webp"
          :on-change="onFileChange"
        >
          <div v-if="previewUrl" class="preview">
            <img :src="previewUrl" alt="预览" />
          </div>
          <div v-else class="upload-tip">
            <p>点击或拖拽上传</p>
            <p class="hint">支持 JPEG / PNG / GIF / WebP，最大 10MB</p>
          </div>
        </el-upload>
      </el-form-item>

      <el-button type="primary" class="full" size="large" :loading="loading" :disabled="!file" @click="onDetect">
        开始识别
      </el-button>
      <el-button class="full mt" @click="$router.push('/user/history')">查看识别历史</el-button>
    </el-form>
  </div>
</template>

<script setup>
/**
 * 识别上传页（阶段9完整实现）。
 * 用例：upload → detectDisease；顺序图：选图→上传→识别→跳转结果页。
 */
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { cropList } from '@/api/content'
import { detectDisease, uploadFile } from '@/api/detection'

const router = useRouter()
const crops = ref([])
const cropId = ref(null)
const file = ref(null)
const previewUrl = ref('')
const imagePath = ref('')
const loading = ref(false)

onMounted(async () => {
  const res = await cropList()
  crops.value = res.data || []
})

function onFileChange(uploadFile) {
  const raw = uploadFile.raw
  if (!raw) return
  const okTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'image/jpg']
  if (!okTypes.includes(raw.type)) {
    ElMessage.error('仅支持 JPEG/PNG/GIF/WebP')
    return
  }
  if (raw.size > 10 * 1024 * 1024) {
    ElMessage.error('图片不能超过 10MB')
    return
  }
  file.value = raw
  previewUrl.value = URL.createObjectURL(raw)
  imagePath.value = ''
}

async function onDetect() {
  if (!file.value) {
    ElMessage.warning('请先选择图片')
    return
  }
  loading.value = true
  try {
    if (!imagePath.value) {
      const up = await uploadFile(file.value)
      imagePath.value = up.data.path
    }
    const res = await detectDisease({
      imagePath: imagePath.value,
      cropId: cropId.value || null
    })
    ElMessage.success('识别完成')
    sessionStorage.setItem('ynzz_last_detect', JSON.stringify(res.data))
    router.push(`/user/detect/result/${res.data.id}`)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page-head h2 { margin: 0 0 0.35rem; color: #1b4332; }
.page-head p { margin: 0 0 1rem; color: #52796f; font-size: 0.92rem; }
.uploader { width: 100%; }
.uploader :deep(.el-upload),
.uploader :deep(.el-upload-dragger) {
  width: 100%;
}
.upload-tip {
  padding: 2rem 1rem;
  color: #52796f;
}
.hint { font-size: 0.8rem; color: #8aa396; }
.preview img {
  max-width: 100%;
  max-height: 260px;
  object-fit: contain;
  display: block;
  margin: 0 auto;
}
.full { width: 100%; }
.mt { margin-top: 0.6rem; }
</style>
