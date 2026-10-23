<template>
  <div class="auth-page">
    <div class="auth-panel">
      <h1>云农智诊</h1>
      <p class="sub">农作物病虫害识别与诊断平台</p>
      <el-form :model="form" @submit.prevent="onSubmit">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名 / 手机号 / 邮箱" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" show-password placeholder="密码" size="large" />
        </el-form-item>
        <el-button type="primary" size="large" class="full" :loading="loading" native-type="submit">登录</el-button>
      </el-form>
      <div class="links">
        <router-link to="/register">注册账号</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useUserStore } from '@/store/user'

const store = useUserStore()
const router = useRouter()
const route = useRoute()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

async function onSubmit() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    const res = await login(form)
    store.setSession(res.data.token, res.data.user)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect
    if (store.isAdmin) {
      router.replace(typeof redirect === 'string' && redirect.startsWith('/admin') ? redirect : '/admin/dashboard')
    } else {
      router.replace(typeof redirect === 'string' ? redirect : '/user/home')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 1.5rem;
  background:
    radial-gradient(circle at 20% 20%, rgba(82, 183, 136, 0.25), transparent 40%),
    linear-gradient(160deg, #e9f5ee, #ffffff 55%);
}
.auth-panel {
  width: min(420px, 100%);
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid #d8e8dc;
  border-radius: 16px;
  padding: 2rem 1.5rem;
}
h1 {
  margin: 0;
  text-align: center;
  color: #1b4332;
  letter-spacing: 0.12em;
}
.sub {
  text-align: center;
  color: #52796f;
  margin: 0.4rem 0 1.5rem;
}
.full { width: 100%; }
.links { margin-top: 1rem; text-align: center; }
.links a { color: #2d6a4f; }
</style>
