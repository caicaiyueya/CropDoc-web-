import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

/**
 * 路由：用户端 /login + /user/**；管理员端 /admin/login + /admin/**。
 * 守卫：后台未登录 → /admin/login；用户端未登录 → /login。
 */
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { public: true, title: '注册' }
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/AdminLogin.vue'),
    meta: { public: true, adminLogin: true, title: '管理员登录' }
  },
  {
    path: '/',
    redirect: '/user/home'
  },
  {
    path: '/ai-chat',
    redirect: (to) => ({ path: '/user/ai-chat', query: to.query })
  },
  { path: '/diseases', redirect: '/user/disease' },
  { path: '/news', redirect: '/user/news' },
  { path: '/community', redirect: '/user/community' },
  { path: '/community/post', redirect: '/user/community/post' },
  {
    path: '/user',
    component: () => import('@/layouts/UserLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: 'home', name: 'UserHome', component: () => import('@/views/user/Home.vue'), meta: { title: '首页' } },
      { path: 'detect', name: 'DetectUpload', component: () => import('@/views/user/AiPredict.vue'), meta: { title: '病虫害识别' } },
      { path: 'detect-legacy', name: 'DetectUploadLegacy', component: () => import('@/views/user/DetectUpload.vue'), meta: { title: '病虫害识别(旧版)' } },
      { path: 'detect/result/:id?', name: 'DetectResult', component: () => import('@/views/user/DetectResult.vue'), meta: { title: '识别结果' } },
      { path: 'ai', name: 'AiConsult', component: () => import('@/views/user/DiagnosisChat.vue'), meta: { title: 'AI问诊' } },
      { path: 'ai-chat', name: 'AiChat', component: () => import('@/views/user/DiagnosisChat.vue'), meta: { title: 'AI问诊' } },
      { path: 'diagnosis/:recordId', name: 'DiagnosisChat', component: () => import('@/views/user/DiagnosisChat.vue'), meta: { title: '智能诊断' } },
      { path: 'history', name: 'DetectHistory', component: () => import('@/views/user/DetectHistory.vue'), meta: { title: '诊断历史' } },
      { path: 'community', name: 'CommunityList', component: () => import('@/views/user/CommunityList.vue'), meta: { title: '农友交流' } },
      { path: 'community/post', name: 'CommunityPostCreate', component: () => import('@/views/user/PostCreate.vue'), meta: { title: '发帖' } },
      { path: 'post/:id', name: 'PostDetail', component: () => import('@/views/user/PostDetail.vue'), meta: { title: '帖子详情' } },
      { path: 'post-create', name: 'PostCreate', component: () => import('@/views/user/PostCreate.vue'), meta: { title: '发帖' } },
      { path: 'profile', name: 'Profile', component: () => import('@/views/user/Profile.vue'), meta: { title: '个人中心' } },
      { path: 'certification', name: 'CertificationApply', component: () => import('@/views/user/CertificationApply.vue'), meta: { title: '认证申请', farmerOnly: true } },
      { path: 'news', name: 'NewsList', component: () => import('@/views/user/NewsList.vue'), meta: { title: '资讯' } },
      { path: 'news/:id', name: 'NewsDetail', component: () => import('@/views/user/NewsDetail.vue'), meta: { title: '资讯详情' } },
      { path: 'disease', name: 'DiseaseList', component: () => import('@/views/user/DiseaseList.vue'), meta: { title: '病害库' } }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '控制台' }
      },
      {
        path: 'users',
        name: 'UserManage',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理', parent: '用户与权限' }
      },
      {
        path: 'certification',
        name: 'CertificationReview',
        component: () => import('@/views/admin/CertificationReview.vue'),
        meta: { title: '认证审核', parent: '用户与权限' }
      },
      {
        path: 'posts',
        name: 'PostReview',
        component: () => import('@/views/admin/PostReview.vue'),
        meta: { title: '帖子审核', parent: '社区管理' }
      },
      {
        path: 'disease',
        name: 'DiseaseManage',
        component: () => import('@/views/admin/DiseaseManage.vue'),
        meta: { title: '病害库管理', parent: '资讯管理' }
      },
      {
        path: 'news',
        name: 'NewsManage',
        component: () => import('@/views/admin/NewsManage.vue'),
        meta: { title: '资讯管理', parent: '资讯管理' }
      },
      // Banner/公告已下线；旧路径重定向
      { path: 'site', redirect: '/admin/news' },
      { path: 'banner', redirect: '/admin/news' },
      {
        path: 'feedback',
        name: 'FeedbackManage',
        component: () => import('@/views/admin/FeedbackManage.vue'),
        meta: { title: '反馈处理' }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/user/home' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

function isAdminPath(path) {
  return path === '/admin' || path.startsWith('/admin/')
}

router.beforeEach((to, from, next) => {
  const store = useUserStore()
  document.title = to.meta.title ? `${to.meta.title} - 云农智诊` : '云农智诊'

  if (to.meta.public) {
    if (store.isLogin) {
      // 仅已登录的管理员访问后台登录页时跳过；普通用户可停留以便切换账号
      if (to.meta.adminLogin) {
        return store.isAdmin ? next('/admin/dashboard') : next()
      }
      if (to.path === '/login' || to.path === '/register') {
        // 管理员访问用户登录页时引导回后台；普通用户已登录则进首页
        if (store.isAdmin) return next('/admin/dashboard')
        return next('/user/home')
      }
    }
    return next()
  }

  if (!store.isLogin) {
    if (isAdminPath(to.path)) {
      return next({ path: '/admin/login', query: { redirect: to.fullPath } })
    }
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  if (to.meta.requiresAdmin && !store.isAdmin) {
    return next('/user/home')
  }

  if (store.isAdmin && to.path.startsWith('/user')) {
    // 管理员可进用户端浏览，不强制拦截
  }

  if (to.meta.farmerOnly && !store.isFarmer) {
    return next('/user/profile')
  }

  next()
})

export default router
