<template>
  <div class="page">
    <h2>个人中心</h2>
    <el-descriptions :column="1" border class="mb">
      <el-descriptions-item label="账号">{{ form.username }}</el-descriptions-item>
      <el-descriptions-item label="角色">{{ form.role }}</el-descriptions-item>
    </el-descriptions>
    <el-form label-position="top">
      <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
      <el-form-item label="地区"><el-input v-model="form.region" /></el-form-item>
      <el-form-item label="联系方式"><el-input v-model="form.contact" /></el-form-item>
      <el-form-item label="头像路径"><el-input v-model="form.avatar" /></el-form-item>
      <el-button type="primary" @click="save">保存</el-button>
    </el-form>
    <el-divider />
    <h3>意见反馈</h3>
    <el-input v-model="feedback" type="textarea" :rows="3" placeholder="说说你的建议" />
    <el-button class="mt" @click="sendFeedback">提交反馈</el-button>
    <el-button class="mt" type="danger" plain @click="logout">退出登录</el-button>
    <el-button v-if="store.isAdmin" class="mt" type="success" plain @click="$router.push('/admin/dashboard')">进入管理后台</el-button>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createFeedback, getProfile, updateProfile } from '@/api/content'
import { useUserStore } from '@/store/user'

const store = useUserStore()
const router = useRouter()
const form = reactive({ username: '', role: '', nickname: '', region: '', contact: '', avatar: '' })
const feedback = ref('')

onMounted(async () => {
  const res = await getProfile()
  Object.assign(form, res.data)
})

async function save() {
  const res = await updateProfile({
    nickname: form.nickname,
    region: form.region,
    contact: form.contact,
    avatar: form.avatar
  })
  store.setUser(res.data)
  ElMessage.success('已保存')
}

async function sendFeedback() {
  if (!feedback.value.trim()) return
  await createFeedback({ content: feedback.value.trim() })
  feedback.value = ''
  ElMessage.success('反馈已提交')
}

function logout() {
  store.logout()
  router.push('/login')
}
</script>

<style scoped>
h2, h3 { color: #1b4332; }
.mb { margin-bottom: 1rem; }
.mt { margin-top: 0.6rem; margin-right: 0.5rem; }
</style>
