<template>
  <div class="auth-page">
    <div class="auth-panel">
      <h1>云农智诊</h1>
      <p class="sub">注册成为普通农户</p>
      <el-form :model="form" @submit.prevent="onSubmit">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名 / 手机号 / 邮箱" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.nickname" placeholder="昵称（可选）" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" show-password placeholder="密码（至少6位）" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="确认密码" size="large" />
        </el-form-item>
        <el-button type="primary" size="large" class="full" :loading="loading" native-type="submit">注册</el-button>
      </el-form>
      <div class="links">
        <router-link to="/login">已有账号？去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: '', nickname: '', password: '', confirmPassword: '' })

async function onSubmit() {
  if (!form.username || !form.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }
  if (form.password !== form.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }
  loading.value = true
  try {
    await register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
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
  background: linear-gradient(160deg, #e9f5ee, #ffffff 55%);
}
.auth-panel {
  width: min(420px, 100%);
  background: #fff;
  border: 1px solid #d8e8dc;
  border-radius: 16px;
  padding: 2rem 1.5rem;
}
h1 { margin: 0; text-align: center; color: #1b4332; letter-spacing: 0.12em; }
.sub { text-align: center; color: #52796f; margin: 0.4rem 0 1.5rem; }
.full { width: 100%; }
.links { margin-top: 1rem; text-align: center; }
.links a { color: #2d6a4f; }
</style>
