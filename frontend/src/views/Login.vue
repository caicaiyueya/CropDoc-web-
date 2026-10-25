<template>
  <div class="auth-page">
    <div class="auth-card">
      <header class="auth-hero">
        <div class="auth-hero-mask" aria-hidden="true" />
        <div class="auth-hero-content">
          <img class="brand-logo" src="/images/logo.png" alt="云农智诊" />
          <h1>云农智诊</h1>
          <p class="slogan">AI助力智慧农业</p>
        </div>
      </header>

      <div class="auth-body">
        <h2 class="form-title">用户登录</h2>
        <el-form :model="form" label-position="top" @submit.prevent="onSubmit">
          <el-form-item label="账号">
            <el-input
              v-model="form.username"
              placeholder="用户名 / 手机号 / 邮箱"
              clearable
              autocomplete="username"
            />
          </el-form-item>
          <el-form-item label="密码">
            <el-input
              v-model="form.password"
              type="password"
              show-password
              placeholder="请输入密码"
              autocomplete="current-password"
            />
          </el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            native-type="submit"
          >
            登录
          </el-button>
        </el-form>
        <div class="links">
          <router-link to="/register">注册账号</router-link>
          <span class="sep">·</span>
          <router-link to="/admin/login">管理员入口</router-link>
        </div>
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
  if (loading.value) return
  loading.value = true
  try {
    const res = await login(form)
    const user = res.data?.user
    if (user?.role === 'ADMIN') {
      ElMessage.warning('管理员请使用后台登录入口')
      store.logout()
      router.replace('/admin/login')
      return
    }
    store.setSession(res.data.token, user)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect
    router.replace(
      typeof redirect === 'string' && !redirect.startsWith('/admin') ? redirect : '/user/home'
    )
  } catch {
    // 错误提示已由 request 拦截器处理
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
    radial-gradient(circle at 18% 12%, rgba(82, 183, 136, 0.22), transparent 42%),
    radial-gradient(circle at 82% 88%, rgba(45, 106, 79, 0.12), transparent 40%),
    linear-gradient(160deg, #e8f5ee, #f4faf6 50%, #eef6f1);
}

.auth-card {
  width: min(400px, 100%);
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 12px 40px rgba(27, 67, 50, 0.12);
}

.auth-hero {
  position: relative;
  height: 168px;
  background: url('/images/hero-farm.jpg') center / cover no-repeat;
  display: flex;
  align-items: center;
  justify-content: center;
}

.auth-hero-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.38);
  pointer-events: none;
}

.auth-hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: #fff;
}

.brand-logo {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  object-fit: cover;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
  margin-bottom: 10px;
}

.auth-hero-content h1 {
  margin: 0;
  font-size: 1.45rem;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.35);
}

.slogan {
  margin: 6px 0 0;
  font-size: 0.82rem;
  opacity: 0.92;
  letter-spacing: 0.06em;
}

.auth-body {
  padding: 1.5rem 1.6rem 1.4rem;
}

.form-title {
  margin: 0 0 1.1rem;
  text-align: center;
  font-size: 1.1rem;
  font-weight: 700;
  color: #28b446;
}

.auth-body :deep(.el-form-item) {
  margin-bottom: 16px;
}

.auth-body :deep(.el-form-item__label) {
  color: #6b7280;
  font-size: 0.85rem;
  padding-bottom: 4px;
  line-height: 1.4;
}

.auth-body :deep(.el-input__wrapper) {
  height: 40px;
  border-radius: 8px;
  box-shadow: 0 0 0 1px #e5e7eb inset;
}

.auth-body :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #86d89a inset;
}

.auth-body :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #28b446 inset !important;
}

.login-btn {
  width: 100%;
  height: 42px;
  margin-top: 4px;
  border-radius: 8px;
  font-weight: 700;
  font-size: 0.95rem;
  background: #28b446;
  border-color: #28b446;
}

.login-btn:hover,
.login-btn:focus {
  background: #22a03d;
  border-color: #22a03d;
}

.links {
  margin-top: 1rem;
  text-align: center;
}

.links a {
  color: #2d6a4f;
  font-size: 0.88rem;
}

.sep {
  margin: 0 0.4rem;
  color: #95a99a;
}
</style>
