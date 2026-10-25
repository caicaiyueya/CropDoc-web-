<template>
  <div class="detail-page" v-loading="loading">
    <template v-if="post">
      <button type="button" class="back-link" @click="$router.push('/user/community')">
        ← 返回农友交流
      </button>

      <!-- 帖子主体 -->
      <article class="card post-card">
        <div v-if="displayTags.length" class="title-row">
          <el-tag
            v-for="t in displayTags"
            :key="t.label"
            size="small"
            effect="light"
            round
            :type="t.type"
            class="tag"
          >
            {{ t.label }}
          </el-tag>
        </div>
        <h1>{{ post.title }}</h1>
        <div class="meta">
          <el-avatar :size="40" class="avatar" :src="authorAvatar || undefined">
            {{ authorLetter }}
          </el-avatar>
          <div class="meta-text">
            <span class="author">{{ authorName }}</span>
            <span class="sub">
              <span v-if="authorRoleLabel">{{ authorRoleLabel }} · </span>
              <time>{{ formatTime(post.createTime) }}</time>
              <span v-if="post.viewCount != null"> · 浏览 {{ post.viewCount }}</span>
            </span>
          </div>
          <el-button class="btn-report" round @click="onReport">举报</el-button>
        </div>
        <div class="content">{{ displayContent }}</div>
        <div v-if="imageUrls.length" class="gallery">
          <img v-for="(url, i) in imageUrls" :key="i" :src="url" :alt="`配图${i + 1}`" />
        </div>
      </article>

      <!-- 评论区 -->
      <section class="card comments-card">
        <h2 class="comments-title">全部评论({{ comments.length }})</h2>

        <!-- 输入区：列表上方 -->
        <div class="composer">
          <el-input
            v-model="commentText"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            resize="none"
            placeholder="写下你的看法…"
            @keydown.ctrl.enter.exact.prevent="onComment"
          />
          <div class="composer-actions">
            <span class="hint">Ctrl + Enter 快捷发送</span>
            <el-button
              type="primary"
              class="btn-publish"
              round
              :loading="sending"
              @click="onComment"
            >
              发表评论
            </el-button>
          </div>
        </div>

        <div class="divider" />

        <div v-if="comments.length" class="comment-list">
          <div v-for="c in comments" :key="c.id" class="comment">
            <el-avatar :size="40" class="avatar" :src="c.avatar || undefined">
              {{ (c.authorName || '农').slice(0, 1) }}
            </el-avatar>
            <div class="c-body">
              <div class="c-head">
                <strong>{{ c.authorName || `农友${c.authorId || ''}` }}</strong>
                <el-tag
                  v-if="c.authorRole === 'EXPERT' || c.isProfessional"
                  size="small"
                  type="warning"
                  effect="light"
                  round
                >
                  专家
                </el-tag>
                <el-tag v-if="c.isQuality" size="small" type="success" effect="light" round>
                  优质
                </el-tag>
                <time>{{ formatTime(c.createTime) }}</time>
                <el-button
                  v-if="store.isExpert && !String(c.id).startsWith('local-')"
                  link
                  type="primary"
                  @click="onMark(c.id)"
                >
                  标优质
                </el-button>
              </div>
              <p>{{ c.content }}</p>
            </div>
          </div>
        </div>
        <p v-else class="empty-tip">暂无评论，快来抢沙发吧~</p>
      </section>
    </template>

    <div v-else-if="!loading" class="card empty-card">
      <el-empty description="帖子不存在或已下线">
        <el-button type="primary" round class="btn-publish" @click="$router.push('/user/community')">
          回社区
        </el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createReport, markQuality, postDetail } from '@/api/post'
import { useUserStore } from '@/store/user'
import { unpackPostImages } from '@/utils/postMedia'

const route = useRoute()
const store = useUserStore()

const loading = ref(false)
const sending = ref(false)
const post = ref(null)
const comments = ref([])
const commentText = ref('')

const postId = computed(() => String(route.params.id || ''))

const storageKey = computed(() => `mock_comments_${postId.value}`)

const authorName = computed(
  () => post.value?.authorName || post.value?.nickname || `农友${post.value?.authorId || ''}`
)
const authorLetter = computed(() => String(authorName.value || '农').slice(0, 1))
const authorAvatar = computed(() => resolveAvatar(post.value?.authorAvatar || post.value?.avatar))
const authorRoleLabel = computed(() => {
  const r = post.value?.authorRole
  if (r === 'EXPERT') return '认证农技员'
  if (r === 'ADMIN') return '管理员'
  if (r === 'FARMER') return '普通农户'
  return ''
})

const imageUrls = computed(() => unpackPostImages(post.value?.images).urls)

const displayContent = computed(() => {
  const raw = post.value?.content || ''
  return raw.replace(/^\s*<!--\w+-->\s*/, '')
})

const displayTags = computed(() => {
  if (!post.value) return []
  const tags = []
  const { category } = unpackPostImages(post.value.images)
  if (post.value.tag === 'GUIDE' || post.value.pinned) {
    tags.push({ label: '官方', type: 'success' })
  }
  if (post.value.authorRole === 'EXPERT') {
    tags.push({ label: '专家', type: 'warning' })
  }
  if (category === 'help') {
    tags.push({ label: '求助', type: 'warning' })
  } else if (category === 'experience') {
    tags.push({ label: '经验', type: 'success' })
  }
  return tags
})

function resolveAvatar(path) {
  if (!path) return ''
  if (path.startsWith('http') || path.startsWith('blob:') || path.startsWith('data:') || path.startsWith('/')) {
    return path
  }
  return `/uploads/${path}`
}

function formatTime(t) {
  if (!t) return '—'
  return String(t).replace('T', ' ').slice(0, 16)
}

function formatNow() {
  const d = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function delay(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

function loadLocalComments() {
  try {
    const raw = JSON.parse(localStorage.getItem(storageKey.value) || '[]')
    return Array.isArray(raw) ? raw : []
  } catch {
    return []
  }
}

function persistLocalComments(list) {
  const onlyLocal = list.filter((c) => String(c.id).startsWith('local-'))
  localStorage.setItem(storageKey.value, JSON.stringify(onlyLocal))
}

function mergeComments(baseList) {
  const local = loadLocalComments()
  const seen = new Set(local.map((c) => String(c.id)))
  const rest = (baseList || []).filter((c) => !seen.has(String(c.id)))
  // 本地新评论在前，其余按时间倒序
  return [...local, ...rest]
}

async function load() {
  loading.value = true
  try {
    const res = await postDetail(route.params.id)
    post.value = res.data?.post || null
    comments.value = mergeComments(res.data?.comments || [])
  } catch {
    post.value = null
    comments.value = []
  } finally {
    loading.value = false
  }
}

async function onComment() {
  const text = commentText.value.trim()
  if (!text) {
    ElMessage.warning('请输入评论内容')
    return
  }
  if (sending.value || !post.value) return

  sending.value = true
  try {
    await delay(500)

    const user = store.userInfo || {}
    const newComment = {
      id: `local-${Date.now()}`,
      postId: post.value.id ?? postId.value,
      authorId: user.id,
      authorName: user.nickname || user.username || '我',
      authorRole: user.role || 'FARMER',
      avatar: resolveAvatar(user.avatar),
      content: text,
      isProfessional: store.isExpert,
      isQuality: false,
      createTime: formatNow()
    }

    comments.value = [newComment, ...comments.value]
    persistLocalComments(comments.value)
    commentText.value = ''
    ElMessage.success('评论发表成功')
  } catch {
    ElMessage.error('评论失败，请稍后重试')
  } finally {
    sending.value = false
  }
}

async function onReport() {
  const { value } = await ElMessageBox.prompt('举报原因', '举报帖子').catch(() => ({ value: null }))
  if (!value) return
  try {
    await createReport({ targetType: 'POST', targetId: Number(route.params.id), reason: value })
    ElMessage.success('已提交举报')
  } catch {
    ElMessage.error('举报提交失败')
  }
}

async function onMark(id) {
  try {
    await markQuality(id)
    ElMessage.success('已标记优质')
    const target = comments.value.find((c) => c.id === id)
    if (target) target.isQuality = true
  } catch {
    ElMessage.error('标记失败')
  }
}

onMounted(load)
watch(() => route.params.id, load)
</script>

<style scoped>
.detail-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 0 40px;
  min-height: 60vh;
}

.back-link {
  border: none;
  background: transparent;
  color: #1f8f36;
  font-weight: 650;
  font-size: 0.9rem;
  cursor: pointer;
  padding: 0 0 14px;
}
.back-link:hover {
  text-decoration: underline;
}

.card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 6px 20px rgba(15, 23, 42, 0.05);
  border: none;
}

.post-card {
  padding: 24px 22px 22px;
  margin-bottom: 14px;
}

.title-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

h1 {
  margin: 0 0 16px;
  font-size: 1.45rem;
  line-height: 1.4;
  color: #163b1f;
  font-weight: 780;
}

.meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
}
.avatar {
  background: #eaf8ee;
  color: #1f8f36;
  font-weight: 700;
  flex-shrink: 0;
}
.meta-text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.author {
  font-weight: 700;
  color: #28b446;
  font-size: 0.95rem;
}
.sub {
  font-size: 0.78rem;
  color: #9ca3af;
}
.btn-report {
  flex-shrink: 0;
  --el-button-text-color: #94a3b8;
  --el-button-bg-color: #f8fafc;
  --el-button-border-color: #e5e7eb;
  --el-button-hover-text-color: #64748b;
  --el-button-hover-bg-color: #f1f5f9;
  --el-button-hover-border-color: #e2e8f0;
}

.content {
  white-space: pre-wrap;
  line-height: 1.75;
  color: #374151;
  font-size: 0.98rem;
}

.gallery {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 10px;
}
.gallery img {
  width: 100%;
  height: 160px;
  object-fit: cover;
  border-radius: 12px;
  background: #eef2f0;
  display: block;
}

.comments-card {
  padding: 20px 22px 24px;
}
.comments-title {
  margin: 0 0 16px;
  font-size: 1.05rem;
  color: #163b1f;
  font-weight: 750;
}

.composer {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.composer-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.hint {
  font-size: 0.78rem;
  color: #9ca3af;
}
.btn-publish {
  --el-button-bg-color: #28b446;
  --el-button-border-color: #28b446;
  --el-button-hover-bg-color: #1f8f36;
  --el-button-hover-border-color: #1f8f36;
  font-weight: 700;
  padding: 10px 22px;
}

.divider {
  height: 1px;
  background: #eef2f0;
  margin: 18px 0;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.comment {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  background: #f8faf9;
}
.c-body {
  flex: 1;
  min-width: 0;
}
.c-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.c-head strong {
  color: #1f2937;
  font-size: 0.9rem;
}
.c-head time {
  color: #9ca3af;
  font-size: 0.75rem;
  margin-left: auto;
}
.c-body p {
  margin: 0;
  color: #4b5563;
  line-height: 1.6;
  font-size: 0.9rem;
  white-space: pre-wrap;
}

.empty-tip {
  margin: 8px 0 0;
  text-align: center;
  color: #9ca3af;
  font-size: 0.9rem;
  padding: 24px 0 8px;
}

.empty-card {
  padding: 48px 20px;
}

@media (max-width: 640px) {
  .post-card,
  .comments-card {
    padding: 18px 14px;
  }
  h1 {
    font-size: 1.2rem;
  }
  .gallery {
    grid-template-columns: 1fr 1fr;
  }
  .composer-actions {
    flex-direction: column;
    align-items: stretch;
  }
  .hint {
    order: 2;
    text-align: center;
  }
}
</style>
