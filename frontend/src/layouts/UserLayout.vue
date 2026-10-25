<template>
  <div class="saas-layout">
    <header class="top-nav">
      <div class="nav-inner">
        <div class="brand" @click="$router.push('/user/home')">
          <img class="logo" src="/images/logo.png" alt="云农智诊" />
          <span class="brand-name">云农智诊</span>
        </div>

        <nav class="menu">
          <router-link
            v-for="item in navMenus"
            :key="item.path"
            :to="item.path"
            class="menu-item"
            active-class="active"
          >
            {{ item.label }}
          </router-link>
        </nav>

        <el-dropdown trigger="click" @command="onCommand">
          <button class="user-btn" type="button">
            <el-avatar :size="34" class="avatar">{{ avatarText }}</el-avatar>
            <span class="user-name">{{ store.userInfo?.nickname || '用户' }}</span>
            <span class="caret">▾</span>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item disabled>
                {{ roleLabel }} · {{ store.userInfo?.username }}
              </el-dropdown-item>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item v-if="store.hasRole('FARMER')" command="cert">提交认证</el-dropdown-item>
              <el-dropdown-item v-if="store.hasRole('EXPERT','ADMIN')" command="guide">专业回答</el-dropdown-item>
              <el-dropdown-item v-if="store.isAdmin" command="admin">管理后台</el-dropdown-item>
              <el-dropdown-item command="history">诊断记录</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <main class="page-main">
      <router-view />
    </main>

    <footer class="site-footer">
      <div class="footer-inner">
        <div class="footer-cols">
          <!-- 第一列：Logo + Slogan -->
          <div class="footer-col brand-col">
            <div class="footer-logo-row">
              <img src="/images/logo.png" alt="" class="footer-logo" />
              <strong>云农智诊</strong>
            </div>
            <p class="slogan">AI助力智慧农业，拍照识病、智能问诊、农友互助一站完成。</p>
          </div>

          <!-- 第二列：核心功能 -->
          <div class="footer-col">
            <h4 class="col-title">核心功能</h4>
            <ul class="col-links">
              <li><router-link to="/user/detect">病虫害识别</router-link></li>
              <li><router-link to="/user/ai">AI问诊</router-link></li>
              <li><router-link to="/user/community">农友交流</router-link></li>
            </ul>
          </div>

          <!-- 第三列：帮助支持 -->
          <div class="footer-col">
            <h4 class="col-title">帮助支持</h4>
            <ul class="col-links">
              <li><a href="#about" @click.prevent>关于我们</a></li>
              <li><a href="#feedback" @click.prevent>意见反馈</a></li>
              <li><a href="#disclaimer" @click.prevent>免责声明</a></li>
            </ul>
          </div>

          <!-- 第四列：联系我们 -->
          <div class="footer-col contact-col">
            <h4 class="col-title">联系我们</h4>
            <div class="contact-info">
              <p class="hotline-label">服务热线</p>
              <p class="hotline">400-828-2024</p>
              <p class="hotline-hint">工作日 09:00–18:00</p>
            </div>
          </div>
        </div>

        <div class="footer-bottom">
          <p>Copyright © 2026 云农智诊</p>
          <p class="disclaimer">AI识别与建议仅供参考，严重情况请咨询当地农技部门</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const store = useUserStore()
const router = useRouter()

const navMenus = [
  { path: '/user/home', label: '首页' },
  { path: '/user/detect', label: '病虫害识别' },
  { path: '/user/ai', label: 'AI问诊' },
  { path: '/user/community', label: '农友交流' }
]

const roleLabel = computed(() => {
  const map = { FARMER: '普通农户', EXPERT: '认证农技员', ADMIN: '管理员' }
  return map[store.role] || store.role
})

const avatarText = computed(() => {
  const name = store.userInfo?.nickname || store.userInfo?.username || '农'
  return String(name).slice(0, 1)
})

function onCommand(cmd) {
  if (cmd === 'profile') router.push('/user/profile')
  else if (cmd === 'cert') router.push('/user/certification')
  else if (cmd === 'guide') router.push('/user/community/post')
  else if (cmd === 'admin') router.push('/admin/dashboard')
  else if (cmd === 'history') router.push('/user/history')
  else if (cmd === 'logout') {
    store.logout()
    router.replace('/login')
  }
}
</script>

<style scoped>
.saas-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg);
}
.top-nav {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
}
.nav-inner {
  max-width: 1360px;
  margin: 0 auto;
  height: 68px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  gap: 28px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  flex-shrink: 0;
}
.logo {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  object-fit: cover;
  box-shadow: 0 4px 12px rgba(40, 180, 70, 0.25);
}
.brand-name {
  font-size: 1.15rem;
  font-weight: 700;
  color: #163b1f;
  letter-spacing: 0.04em;
}
.menu {
  flex: 1;
  display: flex;
  justify-content: center;
  gap: 8px;
}
.menu-item {
  padding: 8px 16px;
  border-radius: 999px;
  color: #4b5563;
  font-size: 0.95rem;
  font-weight: 500;
  transition: 0.2s ease;
}
.menu-item:hover { color: var(--brand); background: var(--brand-soft); }
.menu-item.active {
  color: var(--brand);
  background: var(--brand-soft);
  font-weight: 700;
}
.user-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 999px;
  padding: 4px 12px 4px 4px;
  cursor: pointer;
}
.avatar {
  background: var(--brand);
  color: #fff;
  font-weight: 700;
}
.user-name {
  font-size: 0.9rem;
  color: #374151;
  max-width: 88px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.caret { color: #9ca3af; font-size: 0.75rem; }
.page-main {
  flex: 1;
  width: 100%;
  max-width: 1360px;
  margin: 0 auto;
  padding: 24px;
}

/* Footer — 四列落地感 */
.site-footer {
  margin-top: auto;
  background: #f1f5f9;
  border-top: 1px solid #e2e8f0;
}
.footer-inner {
  max-width: 1360px;
  margin: 0 auto;
  padding: 40px 24px 22px;
}
.footer-cols {
  display: grid;
  grid-template-columns: 1.4fr 1fr 1fr 1.3fr;
  gap: 32px 28px;
  padding-bottom: 32px;
  align-items: start;
}
.footer-logo-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.footer-logo {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  object-fit: cover;
}
.brand-col strong {
  font-size: 1.05rem;
  color: #163b1f;
}
.slogan {
  margin: 0;
  color: #64748b;
  font-size: 0.88rem;
  line-height: 1.7;
  max-width: 260px;
}
.col-title {
  margin: 0 0 14px;
  font-size: 0.92rem;
  font-weight: 700;
  color: #1e293b;
  letter-spacing: 0.02em;
}
.col-links {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.col-links a {
  color: #64748b;
  font-size: 0.88rem;
  transition: color 0.15s ease;
}
.col-links a:hover { color: var(--brand); }

.contact-info {
  padding-top: 2px;
}
.hotline-label {
  margin: 0 0 4px;
  font-size: 0.78rem;
  color: #94a3b8;
}
.hotline {
  margin: 0 0 4px;
  font-size: 1.05rem;
  font-weight: 700;
  color: #1e293b;
  letter-spacing: 0.02em;
}
.hotline-hint {
  margin: 0;
  font-size: 0.75rem;
  color: #94a3b8;
}

.footer-bottom {
  border-top: 1px solid #e2e8f0;
  padding-top: 18px;
  text-align: center;
}
.footer-bottom p {
  margin: 0;
  color: #94a3b8;
  font-size: 0.82rem;
  line-height: 1.6;
}
.disclaimer {
  margin-top: 6px !important;
  color: #a8b0bc !important;
  font-size: 0.75rem !important;
}

@media (max-width: 900px) {
  .nav-inner { height: auto; padding: 12px 16px; flex-wrap: wrap; }
  .menu { order: 3; width: 100%; justify-content: flex-start; overflow-x: auto; }
  .user-name { display: none; }
  .page-main { padding: 16px; }
  .footer-cols { grid-template-columns: 1fr 1fr; gap: 24px; }
}
@media (max-width: 560px) {
  .footer-cols { grid-template-columns: 1fr; }
}
</style>
