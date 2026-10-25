import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

/**
 * 路由：用户端 /user/**，管理员端 /admin/**，/login、/register。
 * 守卫：未登录 → /login；/admin/** 仅 ADMIN；按 role 动态菜单在布局中处理。
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
      // 个人中心已移除「我的收藏」入口；收藏 API 仍保留供后端/管理端使用
      // { path: 'favorites', name: 'Favorites', component: () => import('@/views/user/Favorites.vue'), meta: { title: '我的收藏' } },
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
      { path: 'dashboard', name: 'AdminDashboard', component: () => import('@/views/admin/Dashboard.vue'), meta: { title: '工作台' } },
      { path: 'certification', name: 'CertificationReview', component: () => import('@/views/admin/CertificationReview.vue'), meta: { title: '认证审核' } },
      { path: 'posts', name: 'PostReview', component: () => import('@/views/admin/PostReview.vue'), meta: { title: '帖子审核' } },
      { path: 'reports', name: 'ReportManage', component: () => import('@/views/admin/ReportManage.vue'), meta: { title: '举报处理' } },
      { path: 'users', name: 'UserManage', component: () => import('@/views/admin/UserManage.vue'), meta: { title: '用户管理' } },
      { path: 'news', name: 'NewsManage', component: () => import('@/views/admin/NewsManage.vue'), meta: { title: '资讯管理' } },
      { path: 'disease', name: 'DiseaseManage', component: () => import('@/views/admin/DiseaseManage.vue'), meta: { title: '病害库管理' } },
      { path: 'banner', name: 'BannerNotice', component: () => import('@/views/admin/BannerNotice.vue'), meta: { title: 'Banner/公告' } },
      { path: 'feedback', name: 'FeedbackManage', component: () => import('@/views/admin/FeedbackManage.vue'), meta: { title: '反馈处理' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/user/home' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to, from, next) => {
  const store = useUserStore()
  document.title = to.meta.title ? `${to.meta.title} - 云农智诊` : '云农智诊'

  if (to.meta.public) {
    if (store.isLogin && (to.path === '/login' || to.path === '/register')) {
      return next(store.isAdmin ? '/admin/dashboard' : '/user/home')
    }
    return next()
  }

  if (!store.isLogin) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  if (to.meta.requiresAdmin && !store.isAdmin) {
    return next('/user/home')
  }

  if (to.meta.farmerOnly && !store.isFarmer) {
    return next('/user/profile')
  }

  next()
})

export default router
