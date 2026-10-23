<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="aside">
      <div class="brand">
        <span class="logo-mark"></span>
        <div>
          <div class="name">云农智诊</div>
          <div class="sub">管理后台</div>
        </div>
      </div>
      <el-menu :default-active="$route.path" router background-color="#1b4332" text-color="#d8f3dc" active-text-color="#95d5b2">
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          {{ item.label }}
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>{{ $route.meta.title || '工作台' }}</span>
        <div class="right">
          <span>{{ store.userInfo?.nickname || '管理员' }}</span>
          <el-button link type="primary" @click="goUser">用户端</el-button>
          <el-button link type="danger" @click="onLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const store = useUserStore()
const router = useRouter()

const menus = [
  { path: '/admin/dashboard', label: '工作台 / 数据概览' },
  { path: '/admin/certification', label: '认证审核' },
  { path: '/admin/posts', label: '帖子审核' },
  { path: '/admin/reports', label: '举报处理' },
  { path: '/admin/users', label: '用户管理' },
  { path: '/admin/news', label: '资讯管理' },
  { path: '/admin/disease', label: '病害库管理' },
  { path: '/admin/banner', label: 'Banner / 公告' },
  { path: '/admin/feedback', label: '反馈处理' }
]

function goUser() {
  router.push('/user/home')
}

function onLogout() {
  store.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  background: #f3f6f4;
}
.aside {
  background: #1b4332;
  min-height: 100vh;
}
.brand {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  padding: 1.25rem 1rem;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.logo-mark {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #52b788, #95d5b2);
}
.name {
  font-weight: 700;
  letter-spacing: 0.08em;
}
.sub {
  font-size: 0.75rem;
  opacity: 0.75;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4ece6;
  font-weight: 600;
  color: #1b4332;
}
.right {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}
.main {
  padding: 1.25rem;
}
@media (max-width: 900px) {
  .aside {
    width: 64px !important;
  }
  .brand .name,
  .brand .sub,
  .el-menu-item {
    font-size: 0;
  }
}
</style>
