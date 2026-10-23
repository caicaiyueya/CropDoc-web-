<template>
  <div class="page">
    <h2>{{ store.isExpert ? '发帖 / 专业回答' : '发帖' }}</h2>
    <el-form label-position="top">
      <el-form-item label="标题">
        <el-input v-model="form.title" maxlength="200" />
      </el-form-item>
      <el-form-item label="内容">
        <el-input v-model="form.content" type="textarea" :rows="6" />
      </el-form-item>
      <el-form-item v-if="store.isExpert" label="类型">
        <el-radio-group v-model="form.asGuide">
          <el-radio :label="false">普通帖</el-radio>
          <el-radio :label="true">农技指导（GUIDE）</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-button type="primary" :loading="loading" class="full" @click="onSubmit">发布</el-button>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createGuidePost, createPost } from '@/api/post'
import { useUserStore } from '@/store/user'

const store = useUserStore()
const router = useRouter()
const loading = ref(false)
const form = reactive({ title: '', content: '', asGuide: false })

async function onSubmit() {
  if (!form.title.trim() || !form.content.trim()) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  loading.value = true
  try {
    if (form.asGuide && store.isExpert) {
      await createGuidePost({ title: form.title, content: form.content })
    } else {
      await createPost({ title: form.title, content: form.content, tag: 'NORMAL' })
    }
    ElMessage.success('已提交，等待审核')
    router.push('/user/community')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
h2 { color: #1b4332; }
.full { width: 100%; }
</style>
