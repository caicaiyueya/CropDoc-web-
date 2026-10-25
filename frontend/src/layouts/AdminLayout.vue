<template>
  <el-container class="admin-layout">
    <el-aside width="232px" class="aside">
      <div class="brand">
        <img class="brand-logo" src="/images/logo.png" alt="云农智诊" />
        <div>
          <div class="name">云农智诊</div>
          <div class="sub">管理后台</div>
        </div>
      </div>
      <el-menu
        :default-active="activeMenu"
        :default-openeds="defaultOpeneds"
        router
        background-color="#1b4332"
        text-color="#d8f3dc"
        active-text-color="#95d5b2"
        class="side-menu"
      >
        <el-menu-item index="/admin/dashboard">
          <span>控制台</span>
        </el-menu-item>

        <el-sub-menu index="user-auth">
          <template #title><span>用户与权限</span></template>
          <el-menu-item index="/admin/users">用户管理</el-menu-item>
          <el-menu-item index="/admin/certification">认证审核</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="community">
          <template #title><span>社区管理</span></template>
          <el-menu-item index="/admin/posts">帖子审核</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="news-group">
          <template #title><span>资讯管理</span></template>
          <el-menu-item index="/admin/disease">病害库管理</el-menu-item>
          <el-menu-item index="/admin/news">资讯管理</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/admin/feedback">
          <span>反馈处理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item
            v-for="(b, i) in breadcrumbs"
            :key="i"
            :to="b.path || undefined"
          >
            {{ b.title }}
          </el-breadcrumb-item>
        </el-breadcrumb>
        <div class="right">
          <img class="header-logo" src="/images/logo.png" alt="云农智诊" />
          <span class="admin-name">{{ store.userInfo?.nickname || '系统管理员' }}</span>
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
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const store = useUserStore()
const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)

const defaultOpeneds = ['user-auth', 'community', 'news-group']

const breadcrumbs = computed(() => {
  const parent = route.meta?.parent
  const title = route.meta?.title || '控制台'
  const crumbs = [{ title: '管理后台', path: '/admin/dashboard' }]
  if (parent && parent !== title) crumbs.push({ title: parent })
  if (title) crumbs.push({ title })
  return crumbs
})

function onLogout() {
  store.logout()
  router.push('/admin/login')
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
.brand-logo {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  object-fit: contain;
  background: #fff;
  padding: 4px;
  flex-shrink: 0;
}
.name {
  font-weight: 700;
  letter-spacing: 0.08em;
}
.sub {
  font-size: 0.75rem;
  opacity: 0.75;
}
.side-menu {
  border-right: none;
}
.side-menu :deep(.el-sub-menu__title),
.side-menu :deep(.el-menu-item) {
  height: 46px;
  line-height: 46px;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4ece6;
  height: 56px;
  padding: 0 24px;
}
.right {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}
.header-logo {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  object-fit: contain;
  background: #eaf8ee;
  padding: 3px;
}
.admin-name {
  color: #1b4332;
  font-weight: 600;
  font-size: 0.9rem;
}
.main {
  padding: 24px;
  box-sizing: border-box;
}
.main :deep(.admin-page) {
  max-width: 1400px;
  margin: 0 auto;
}
@media (max-width: 900px) {
  .aside {
    width: 64px !important;
  }
  .brand .name,
  .brand .sub {
    display: none;
  }
}
</style>
