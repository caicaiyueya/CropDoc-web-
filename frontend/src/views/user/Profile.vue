<template>
  <div class="profile-page">
    <header class="page-head">
      <div class="head-text">
        <h1>个人中心</h1>
        <p>管理资料、查看近期诊断与意见反馈</p>
      </div>
      <button type="button" class="btn-logout head-logout" @click="logout">退出登录</button>
    </header>

    <div class="profile-grid">
      <!-- 左侧 30%：个人资料 -->
      <aside class="card profile-card">
        <div class="avatar-block">
          <button
            type="button"
            class="avatar-btn"
            title="点击更换头像"
            @click="triggerAvatarPick"
          >
            <img v-if="avatarSrc" :src="avatarSrc" alt="头像" class="avatar-img" />
            <span v-else class="avatar-fallback">{{ avatarLetter }}</span>
            <span class="avatar-overlay">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
                <path d="M4 8.5A2.5 2.5 0 0 1 6.5 6h2.1l1.2-1.6A1.5 1.5 0 0 1 11 4h2a1.5 1.5 0 0 1 1.2.4L15.4 6h2.1A2.5 2.5 0 0 1 20 8.5v9A2.5 2.5 0 0 1 17.5 20h-11A2.5 2.5 0 0 1 4 17.5v-9z" stroke-linejoin="round"/>
                <circle cx="12" cy="13" r="3.2"/>
              </svg>
            </span>
          </button>
          <button type="button" class="change-avatar" @click="triggerAvatarPick" :disabled="avatarUploading">
            {{ avatarUploading ? '上传中…' : '更换头像' }}
          </button>
          <input
            ref="fileInput"
            type="file"
            accept="image/jpeg,image/png,image/gif,image/webp"
            class="file-hidden"
            @change="onAvatarSelected"
          />
        </div>

        <form class="profile-form" @submit.prevent="save">
          <label class="field">
            <span class="label">昵称</span>
            <input v-model="form.nickname" type="text" class="input" placeholder="请输入昵称" />
          </label>
          <label class="field">
            <span class="label">账号</span>
            <input :value="form.username" type="text" class="input is-readonly" readonly disabled />
          </label>
          <label class="field">
            <span class="label">角色</span>
            <input :value="roleLabel" type="text" class="input is-readonly" readonly disabled />
          </label>
          <label class="field">
            <span class="label">地区</span>
            <input v-model="form.region" type="text" class="input" placeholder="如：湖南省长沙市" />
          </label>
          <label class="field">
            <span class="label">联系方式</span>
            <input v-model="form.contact" type="text" class="input" placeholder="手机号或邮箱" />
          </label>

          <button type="submit" class="btn-save" :disabled="saving">
            {{ saving ? '保存中…' : '保存修改' }}
          </button>
        </form>

        <button
          v-if="store.isAdmin"
          type="button"
          class="btn-admin"
          @click="$router.push('/admin/dashboard')"
        >
          进入管理后台
        </button>
      </aside>

      <!-- 右侧 70%：与左侧等高对齐 -->
      <section class="right-col">
        <div class="shortcut-row">
          <button
            v-for="item in shortcuts"
            :key="item.key"
            type="button"
            class="shortcut-chip"
            @click="onShortcut(item)"
          >
            <span class="sc-icon" aria-hidden="true" v-html="item.icon" />
            <div class="sc-text">
              <span class="sc-title">{{ item.title }}</span>
              <span class="sc-desc">{{ item.desc }}</span>
            </div>
          </button>
        </div>

        <div class="card records-card">
          <div class="card-head">
            <h2 class="section-title">最近诊断记录</h2>
            <router-link to="/user/history" class="link-all">查看全部</router-link>
          </div>

          <div class="records-body">
            <button
              v-for="item in recentRecords"
              :key="item.id"
              type="button"
              class="record-row"
              @click="$router.push('/user/history')"
            >
              <div class="thumb">
                <img v-if="thumbUrl(item)" :src="thumbUrl(item)" alt="" />
                <span v-else class="thumb-fallback">病</span>
              </div>
              <div class="record-main">
                <div class="record-top">
                  <span class="disease">{{ item.diseaseName || item.result || '未知病害' }}</span>
                  <span class="sev-tag" :class="severityMeta(item).cls">{{ severityMeta(item).label }}</span>
                </div>
                <time class="record-time">{{ formatTime(item.createTime) }}</time>
              </div>
              <span class="row-arrow" aria-hidden="true">›</span>
            </button>

            <div v-if="!recentRecords.length" class="records-empty">
              <div class="empty-illust" aria-hidden="true">
                <svg viewBox="0 0 120 88" fill="none">
                  <path d="M0 68h120v20H0z" fill="#e8f5e9"/>
                  <path d="M8 68c8-18 18-28 28-28s18 12 28 28" stroke="#86d89a" stroke-width="2.5" fill="none"/>
                  <path d="M52 68c8-22 20-34 32-34s18 16 28 34" stroke="#28b446" stroke-width="2.5" fill="none" opacity="0.7"/>
                  <circle cx="36" cy="32" r="10" fill="#fef9c3" stroke="#facc15" stroke-width="1.5"/>
                  <path d="M78 48c0-8 6-14 12-14s10 5 10 12c0 10-8 18-12 22-4-4-10-12-10-20z" fill="#86d89a"/>
                  <path d="M90 46v24" stroke="#166534" stroke-width="2" stroke-linecap="round"/>
                  <rect x="48" y="40" width="28" height="22" rx="4" stroke="#94a3b8" stroke-width="2" fill="#f8fafc"/>
                  <circle cx="56" cy="48" r="3" stroke="#94a3b8" stroke-width="1.5"/>
                  <path d="M48 56l6-5 5 4 4-3 13 8" stroke="#94a3b8" stroke-width="1.5" stroke-linejoin="round"/>
                </svg>
              </div>
              <p class="empty-title">暂无识别记录</p>
              <p class="empty-desc">拍一张叶片照片，立刻获得 AI 病害研判</p>
              <button type="button" class="btn-go-detect" @click="$router.push('/user/detect')">
                去拍照识病
              </button>
            </div>
          </div>
        </div>

        <div class="card feedback-card">
          <div class="feedback-head">
            <h2 class="section-title sm">意见反馈</h2>
            <span class="feedback-hint">建议将帮助我们持续改进</span>
          </div>
          <textarea
            v-model="feedback"
            class="feedback-area"
            rows="6"
            placeholder="请留下您的宝贵建议…"
          />
          <div class="feedback-actions">
            <button
              type="button"
              class="btn-feedback"
              :disabled="feedbackSending"
              @click="sendFeedback"
            >
              {{ feedbackSending ? '提交中…' : '提交反馈' }}
            </button>
          </div>
        </div>
      </section>
    </div>

    <!-- 页底居中退出（与表单分离） -->
    <div class="page-foot">
      <button type="button" class="btn-logout foot-logout" @click="logout">退出登录</button>
    </div>

    <el-dialog v-model="noticeVisible" title="系统通知" width="480px" destroy-on-close>
      <div v-loading="noticeLoading" class="notice-list">
        <article v-for="n in notices" :key="n.id || n.title" class="notice-item">
          <h4>{{ n.title }}</h4>
          <p>{{ n.content || n.summary || '暂无详情' }}</p>
          <time v-if="n.createTime">{{ formatTime(n.createTime) }}</time>
        </article>
        <el-empty v-if="!noticeLoading && !notices.length" description="暂无系统通知" :image-size="72" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createFeedback, getProfile, noticeList, updateProfile } from '@/api/content'
import { uploadFile } from '@/api/detection'
import { useHistoryStore } from '@/store/history'
import { useUserStore } from '@/store/user'

const store = useUserStore()
const historyStore = useHistoryStore()
const router = useRouter()

const form = reactive({
  username: '',
  role: '',
  nickname: '',
  region: '',
  contact: '',
  avatar: ''
})
const feedback = ref('')
const saving = ref(false)
const feedbackSending = ref(false)
const avatarUploading = ref(false)
const localPreview = ref('')
const fileInput = ref(null)

const noticeVisible = ref(false)
const noticeLoading = ref(false)
const notices = ref([])

/** 与全局 historyStore 实时同步（识别/问诊写入后立刻可见） */
const recentRecords = computed(() => historyStore.getRecords().slice(0, 5))

const ROLE_MAP = {
  FARMER: '普通农户（FARMER）',
  EXPERT: '认证农技员（EXPERT）',
  ADMIN: '管理员（ADMIN）'
}

const roleLabel = computed(() => ROLE_MAP[form.role] || form.role || '—')

const avatarLetter = computed(() => {
  const name = form.nickname || form.username || '农'
  return String(name).slice(0, 1)
})

const avatarSrc = computed(() => {
  if (localPreview.value) return localPreview.value
  return resolveAvatarUrl(form.avatar)
})

const shortcuts = [
  {
    key: 'history',
    title: '我的诊断历史',
    desc: '查看识别与问诊记录',
    path: '/user/history',
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="12" cy="12" r="8"/><path d="M12 8v4.5l3 1.5" stroke-linecap="round"/></svg>`
  },
  {
    key: 'notices',
    title: '系统通知',
    desc: '平台公告与提醒',
    path: null,
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M6 16v-5a6 6 0 1 1 12 0v5"/><path d="M5 16h14M10 19a2 2 0 0 0 4 0" stroke-linecap="round"/></svg>`
  }
]

function resolveAvatarUrl(path) {
  if (!path) return ''
  if (path.startsWith('http') || path.startsWith('blob:') || path.startsWith('data:')) return path
  if (path.startsWith('/')) return path
  return `/uploads/${path}`
}

function thumbUrl(item) {
  if (!item) return ''
  if (item.imageUrl) return item.imageUrl
  if (item.previewUrl) return item.previewUrl
  const p = item.imagePath
  if (!p) return ''
  if (p.startsWith('http') || p.startsWith('/')) return p
  return `/uploads/${p}`
}

function formatTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 16)
}

/** 与识别结果页一致：按置信度映射危害等级彩色标签 */
function severityMeta(item) {
  const name = String(item?.diseaseName || item?.result || '').toLowerCase()
  if (name.includes('healthy') || name.includes('健康')) {
    return { label: '健康', cls: 'ok' }
  }
  if (item?.type === 'AI_CONSULT') {
    return { label: '问诊', cls: 'ok' }
  }
  const c = Number(item?.confidence)
  const v = Number.isNaN(c) ? 0 : c <= 1 ? c : c / 100
  if (v >= 0.85) return { label: '重度', cls: 'severe' }
  if (v >= 0.6) return { label: '中度', cls: 'medium' }
  return { label: '轻度', cls: 'mild' }
}

onMounted(async () => {
  try {
    const res = await getProfile()
    Object.assign(form, res.data || {})
  } catch {
    ElMessage.error('加载个人资料失败')
  }
})

function triggerAvatarPick() {
  fileInput.value?.click()
}

async function onAvatarSelected(e) {
  const file = e.target.files?.[0]
  e.target.value = ''
  if (!file) return
  const okTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'image/jpg']
  if (!okTypes.includes(file.type)) {
    ElMessage.error('仅支持 JPEG / PNG / GIF / WebP')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('头像不能超过 5MB')
    return
  }
  if (localPreview.value) URL.revokeObjectURL(localPreview.value)
  localPreview.value = URL.createObjectURL(file)
  avatarUploading.value = true
  try {
    const up = await uploadFile(file)
    const path = up.data?.path || up.data?.url || ''
    form.avatar = path
    const res = await updateProfile({
      nickname: form.nickname,
      region: form.region,
      contact: form.contact,
      avatar: form.avatar
    })
    store.setUser(res.data)
    Object.assign(form, res.data || {})
    ElMessage.success('头像已更新')
  } catch {
    ElMessage.error('头像上传失败')
  } finally {
    avatarUploading.value = false
  }
}

async function save() {
  saving.value = true
  try {
    const res = await updateProfile({
      nickname: form.nickname,
      region: form.region,
      contact: form.contact,
      avatar: form.avatar
    })
    store.setUser(res.data)
    Object.assign(form, res.data || {})
    ElMessage.success('已保存')
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function sendFeedback() {
  if (!feedback.value.trim()) {
    ElMessage.warning('请先填写反馈内容')
    return
  }
  feedbackSending.value = true
  try {
    await createFeedback({ content: feedback.value.trim() })
    feedback.value = ''
    ElMessage.success('反馈已提交')
  } catch {
    ElMessage.error('提交失败')
  } finally {
    feedbackSending.value = false
  }
}

async function onShortcut(item) {
  if (item.key === 'notices') {
    noticeVisible.value = true
    noticeLoading.value = true
    try {
      const res = await noticeList()
      notices.value = res.data?.records || res.data || []
      if (!Array.isArray(notices.value)) notices.value = []
    } catch {
      notices.value = []
    } finally {
      noticeLoading.value = false
    }
    return
  }
  if (item.path) router.push(item.path)
}

function logout() {
  store.logout()
  router.push('/login')
}
</script>

<style scoped>
.profile-page {
  width: 100%;
  max-width: 1360px;
  margin: 0 auto;
  padding-bottom: 8px;
}

.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}
.head-text h1 {
  margin: 0 0 6px;
  font-size: 1.5rem;
  color: #163b1f;
  font-weight: 750;
}
.head-text p {
  margin: 0;
  color: var(--muted);
  font-size: 0.92rem;
}

.profile-grid {
  display: grid;
  grid-template-columns: minmax(280px, 30%) 1fr;
  gap: 20px;
  align-items: stretch;
}

.card {
  background: #fff;
  border-radius: 12px;
  border: none;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.05);
}

/* —— 左侧资料 —— */
.profile-card {
  padding: 28px 24px 24px;
  display: flex;
  flex-direction: column;
  height: 100%;
}
.avatar-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 22px;
}
.avatar-btn {
  position: relative;
  width: 112px;
  height: 112px;
  border-radius: 50%;
  border: 3px solid #e8f7ec;
  padding: 0;
  overflow: hidden;
  cursor: pointer;
  background: var(--brand-soft);
  box-shadow: 0 8px 20px rgba(40, 180, 70, 0.16);
}
.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.avatar-fallback {
  display: grid;
  place-items: center;
  width: 100%;
  height: 100%;
  font-size: 2.4rem;
  font-weight: 800;
  color: var(--brand);
}
.avatar-overlay {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  background: rgba(15, 23, 42, 0.42);
  color: #fff;
  opacity: 0;
  transition: opacity 0.18s ease;
}
.avatar-overlay svg {
  width: 28px;
  height: 28px;
}
.avatar-btn:hover .avatar-overlay {
  opacity: 1;
}
.change-avatar {
  margin-top: 12px;
  border: none;
  background: none;
  color: #28b446;
  font-size: 0.9rem;
  font-weight: 650;
  cursor: pointer;
  padding: 4px 8px;
}
.change-avatar:hover:not(:disabled) {
  color: var(--brand-dark);
  text-decoration: underline;
}
.change-avatar:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}
.file-hidden {
  display: none;
}

.profile-form {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.label {
  font-size: 0.82rem;
  font-weight: 600;
  color: #64748b;
}
.input {
  width: 100%;
  height: 42px;
  padding: 0 12px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  font-size: 0.92rem;
  color: #1f2937;
  background: #fff;
  outline: none;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}
.input::placeholder {
  color: #9ca3af;
}
.input:hover:not(:disabled):not(.is-readonly) {
  border-color: #c5e8cd;
}
.input:focus:not(:disabled):not(.is-readonly) {
  border-color: #28b446;
  box-shadow: 0 0 0 3px rgba(40, 180, 70, 0.15);
}
.input.is-readonly,
.input:disabled {
  background: #f3f4f6;
  color: #6b7280;
  cursor: not-allowed;
}

.btn-save {
  margin-top: auto;
  width: 100%;
  height: 44px;
  border: none;
  border-radius: 10px;
  background: #28b446;
  color: #fff;
  font-size: 0.98rem;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 6px 16px rgba(40, 180, 70, 0.22);
  transition: background 0.15s ease, transform 0.15s ease;
}
.btn-save:hover:not(:disabled) {
  background: var(--brand-dark);
  transform: translateY(-1px);
}
.btn-save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-admin {
  margin-top: 12px;
  width: 100%;
  height: 40px;
  border-radius: 10px;
  border: 1px solid #c5e8cd;
  background: #f0fdf4;
  color: var(--brand-dark);
  font-weight: 600;
  cursor: pointer;
}
.btn-admin:hover {
  background: #eaf8ee;
}

/* —— 右侧：与左侧等高 —— */
.right-col {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
  height: 100%;
  min-height: 100%;
}

.shortcut-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
  flex-shrink: 0;
}
.shortcut-chip {
  display: flex;
  align-items: center;
  gap: 14px;
  min-height: 96px;
  padding: 22px 18px;
  border: none;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.05);
  cursor: pointer;
  text-align: left;
  transition: box-shadow 0.15s ease, transform 0.15s ease;
}
.shortcut-chip:hover {
  box-shadow: 0 10px 24px rgba(40, 180, 70, 0.14);
  transform: translateY(-2px);
}
.sc-icon {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  color: #28b446;
  background: #eaf8ee;
}
.sc-icon :deep(svg) {
  width: 26px;
  height: 26px;
}
.sc-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}
.sc-title {
  font-size: 1rem;
  font-weight: 750;
  color: #1f2937;
  line-height: 1.3;
}
.sc-desc {
  font-size: 0.78rem;
  color: #94a3b8;
  line-height: 1.4;
}

.section-title {
  margin: 0;
  font-size: 1.05rem;
  color: #163b1f;
  font-weight: 700;
}
.section-title.sm {
  font-size: 0.98rem;
  font-weight: 650;
}

.records-card {
  padding: 18px 20px 16px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}
.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  flex-shrink: 0;
}
.link-all {
  font-size: 0.86rem;
  font-weight: 600;
  color: #28b446;
}
.link-all:hover {
  color: var(--brand-dark);
  text-decoration: underline;
}

.records-body {
  min-height: 200px;
  display: flex;
  flex-direction: column;
}
.record-row {
  width: 100%;
  display: grid;
  grid-template-columns: 56px 1fr auto;
  gap: 14px;
  align-items: center;
  padding: 12px 8px;
  border: none;
  border-bottom: 1px solid #f1f5f9;
  background: transparent;
  cursor: pointer;
  text-align: left;
  transition: background 0.12s ease;
}
.record-row:last-of-type {
  border-bottom: none;
}
.record-row:hover {
  background: #f8faf9;
  border-radius: 10px;
}
.thumb {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  overflow: hidden;
  background: #eef2f0;
  flex-shrink: 0;
}
.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.thumb-fallback {
  display: grid;
  place-items: center;
  width: 100%;
  height: 100%;
  color: #28b446;
  font-weight: 700;
  font-size: 0.95rem;
  background: #eaf8ee;
}
.record-main {
  min-width: 0;
}
.record-top {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.disease {
  font-size: 0.95rem;
  font-weight: 700;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}
.record-time {
  display: block;
  margin-top: 4px;
  font-size: 0.8rem;
  color: #94a3b8;
}
.sev-tag {
  flex-shrink: 0;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 0.72rem;
  font-weight: 700;
  line-height: 1.5;
}
.sev-tag.severe {
  background: #fef2f2;
  color: #dc2626;
}
.sev-tag.medium {
  background: #fff7ed;
  color: #ea580c;
}
.sev-tag.mild {
  background: #fefce8;
  color: #ca8a04;
}
.sev-tag.ok {
  background: #eaf8ee;
  color: #1f8f36;
}
.row-arrow {
  color: #cbd5e1;
  font-size: 1.4rem;
  font-weight: 300;
  padding-right: 4px;
}
.record-row:hover .row-arrow {
  color: #28b446;
}

.records-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 36px 16px 40px;
  text-align: center;
}
.empty-illust {
  width: 120px;
  height: 88px;
  margin-bottom: 14px;
  opacity: 0.95;
}
.empty-illust svg {
  width: 100%;
  height: 100%;
}
.empty-title {
  margin: 0 0 6px;
  font-size: 0.98rem;
  font-weight: 700;
  color: #64748b;
}
.empty-desc {
  margin: 0 0 18px;
  font-size: 0.84rem;
  color: #94a3b8;
  line-height: 1.5;
}
.btn-go-detect {
  height: 42px;
  padding: 0 24px;
  border: none;
  border-radius: 999px;
  background: #28b446;
  color: #fff;
  font-weight: 700;
  font-size: 0.92rem;
  cursor: pointer;
  box-shadow: 0 8px 18px rgba(40, 180, 70, 0.25);
  transition: background 0.15s ease, transform 0.15s ease;
}
.btn-go-detect:hover {
  background: var(--brand-dark);
  transform: translateY(-1px);
}

/* 反馈：弹性拉伸填满右侧剩余高度 */
.feedback-card {
  flex: 1;
  min-height: 0;
  padding: 18px 20px 16px;
  display: flex;
  flex-direction: column;
}
.feedback-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 12px;
  flex-wrap: wrap;
  flex-shrink: 0;
}
.feedback-hint {
  font-size: 0.8rem;
  color: #94a3b8;
}
.feedback-area {
  width: 100%;
  flex: 1;
  min-height: 140px;
  padding: 14px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #f8faf9;
  font-size: 0.92rem;
  line-height: 1.65;
  color: #1f2937;
  resize: none;
  outline: none;
  font-family: inherit;
  transition: border-color 0.15s ease, box-shadow 0.15s ease, background 0.15s ease;
}
.feedback-area::placeholder {
  color: #9ca3af;
}
.feedback-area:hover {
  border-color: #c5e8cd;
}
.feedback-area:focus {
  border-color: #28b446;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(40, 180, 70, 0.12);
}
.feedback-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
  flex-shrink: 0;
}
.btn-feedback {
  min-width: 120px;
  height: 40px;
  padding: 0 18px;
  border: none;
  border-radius: 999px;
  background: #28b446;
  color: #fff;
  font-size: 0.9rem;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 6px 14px rgba(40, 180, 70, 0.2);
}
.btn-feedback:hover:not(:disabled) {
  background: var(--brand-dark);
}
.btn-feedback:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 退出登录：与表单分离 */
.btn-logout {
  height: 38px;
  padding: 0 18px;
  border-radius: 8px;
  border: 1.5px solid #f87171;
  background: #fff;
  color: #dc2626;
  font-size: 0.88rem;
  font-weight: 650;
  cursor: pointer;
  transition: background 0.15s ease, border-color 0.15s ease;
  white-space: nowrap;
}
.btn-logout:hover {
  background: #fef2f2;
  border-color: #ef4444;
}
.head-logout {
  flex-shrink: 0;
  margin-top: 2px;
}
.page-foot {
  display: flex;
  justify-content: center;
  padding: 28px 0 8px;
}
.foot-logout {
  min-width: 140px;
}

.notice-list {
  min-height: 120px;
  max-height: 360px;
  overflow: auto;
}
.notice-item {
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
}
.notice-item:last-child {
  border-bottom: none;
}
.notice-item h4 {
  margin: 0 0 6px;
  font-size: 0.95rem;
  color: #1f2937;
}
.notice-item p {
  margin: 0 0 6px;
  font-size: 0.85rem;
  color: #64748b;
  line-height: 1.55;
}
.notice-item time {
  font-size: 0.75rem;
  color: #94a3b8;
}

@media (min-width: 1025px) {
  .foot-logout {
    display: none;
  }
  .page-foot {
    padding: 12px 0 0;
  }
}
@media (max-width: 1024px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
  .head-logout {
    display: none;
  }
}
@media (max-width: 720px) {
  .shortcut-row {
    grid-template-columns: 1fr;
  }
  .record-row {
    grid-template-columns: 48px 1fr auto;
    gap: 10px;
  }
  .thumb {
    width: 48px;
    height: 48px;
  }
}
</style>
