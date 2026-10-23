<template>
  <div class="user-layout">
    <header class="top-bar">
      <div class="brand" @click="$router.push('/user/home')">
        <span class="logo-mark" aria-hidden="true"></span>
        <span class="brand-name">云农智诊</span>
      </div>
      <div class="user-chip" @click="$router.push('/user/profile')">
        {{ store.userInfo?.nickname || '我的' }}
        <el-tag size="small" type="success" effect="plain">{{ roleLabel }}</el-tag>
      </div>
    </header>

    <main class="main">
      <router-view />
    </main>

    <nav class="tab-bar">
      <router-link
        v-for="item in menus"
        :key="item.path"
        :to="item.path"
        class="tab-item"
        active-class="active"
      >
        <span>{{ item.label }}</span>
      </router-link>
    </nav>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store/user'

const store = useUserStore()

const roleLabel = computed(() => {
  const map = { FARMER: '农户', EXPERT: '农技员', ADMIN: '管理员' }
  return map[store.role] || store.role
})

/** 按 role 动态菜单：FARMER 显示提交认证；EXPERT 显示专业回答且不显示提交认证 */
const menus = computed(() => {
  const base = [
    { path: '/user/home', label: '首页' },
    { path: '/user/detect', label: '识别' },
    { path: '/user/community', label: '社区' },
    { path: '/user/news', label: '资讯' },
    { path: '/user/disease', label: '病害库' }
  ]
  if (store.hasRole('FARMER')) {
    base.push({ path: '/user/certification', label: '提交认证' })
  }
  if (store.hasRole('EXPERT', 'ADMIN')) {
    // EXPERT 显示「专业回答」，不显示「提交认证」
    base.push({ path: '/user/post-create', label: '专业回答' })
  }
  base.push({ path: '/user/profile', label: '我的' })
  return base
})
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #e8f5e9 0%, #f7faf7 28%, #ffffff 100%);
  padding-bottom: 64px;
}
.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.85rem 1rem;
  position: sticky;
  top: 0;
  z-index: 20;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid #d8e8dc;
}
.brand {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
}
.logo-mark {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: linear-gradient(135deg, #2d6a4f, #52b788);
}
.brand-name {
  font-size: 1.2rem;
  font-weight: 700;
  color: #1b4332;
  letter-spacing: 0.06em;
}
.user-chip {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.9rem;
  color: #2d6a4f;
  cursor: pointer;
}
.main {
  flex: 1;
  width: 100%;
  max-width: 720px;
  margin: 0 auto;
  padding: 1rem;
}
.tab-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  overflow-x: auto;
  gap: 0.15rem;
  padding: 0.4rem 0.35rem calc(0.4rem + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1px solid #dce8df;
  z-index: 30;
}
.tab-item {
  flex: 1 0 auto;
  min-width: 3.4rem;
  text-align: center;
  text-decoration: none;
  color: #6b7c72;
  font-size: 0.72rem;
  padding: 0.35rem 0.2rem;
  border-radius: 8px;
}
.tab-item.active {
  color: #1b4332;
  background: #e8f5e9;
  font-weight: 600;
}
</style>
