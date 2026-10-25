<template>
  <div class="community-page">
    <div class="layout">
      <!-- 左 70%：主内容 -->
      <section class="main-col">
        <header class="toolbar card-soft">
          <el-radio-group v-model="sort" size="default" class="sort-tabs" @change="load">
            <el-radio-button label="time">最新</el-radio-button>
            <el-radio-button label="hot">热门</el-radio-button>
          </el-radio-group>
          <el-button type="primary" class="btn-post" round @click="goCreate">
            发帖
          </el-button>
        </header>

        <div v-if="loading" class="post-list">
          <div v-for="i in 4" :key="i" class="post-card card-soft skeleton">
            <el-skeleton animated :rows="3" />
          </div>
        </div>

        <div v-else-if="list.length" class="post-list">
          <article
            v-for="p in list"
            :key="p.id"
            class="post-card card-soft"
            @click="$router.push(`/user/post/${p.id}`)"
          >
            <div v-if="p.cover" class="cover">
              <img :src="p.cover" :alt="p.title" />
            </div>
            <div class="body">
              <div class="title-row">
                <el-tag
                  v-for="t in p.displayTags"
                  :key="t.label"
                  size="small"
                  effect="light"
                  round
                  :type="t.type"
                  class="tag"
                >
                  {{ t.label }}
                </el-tag>
                <h3>{{ p.title }}</h3>
              </div>
              <p class="summary">{{ p.summary }}</p>
              <div class="meta">
                <span class="author">{{ p.authorName }}</span>
                <span class="dot">·</span>
                <time>{{ formatTime(p.createTime) }}</time>
                <span class="dot">·</span>
                <span>浏览 {{ p.viewCount ?? 0 }}</span>
              </div>
            </div>
          </article>
        </div>

        <div v-else class="empty-panel card-soft">
          <el-empty description="暂无帖子，快来发布第一条吧" :image-size="128">
            <el-button type="primary" class="btn-post" round @click="goCreate">去发帖</el-button>
          </el-empty>
        </div>
      </section>

      <!-- 右 30%：侧栏 -->
      <aside class="side-col">
        <div class="side-card card-soft">
          <h4>热门话题</h4>
          <div class="topics">
            <button
              v-for="topic in hotTopics"
              :key="topic"
              type="button"
              class="topic-tag"
              @click="onTopic(topic)"
            >
              # {{ topic }}
            </button>
          </div>
        </div>

        <div class="side-card card-soft">
          <h4>活跃农友</h4>
          <ul class="users">
            <li v-for="u in activeUsers" :key="u.name">
              <el-avatar :size="36" class="avatar">{{ u.name.slice(0, 1) }}</el-avatar>
              <div class="u-meta">
                <strong>{{ u.name }}</strong>
                <span>{{ u.role }}</span>
              </div>
            </li>
          </ul>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { postList } from '@/api/post'
import { SEED_ACTIVE_USERS, SEED_POSTS } from '@/mock/communitySeed'
import { unpackPostImages, categoryLabel } from '@/utils/postMedia'

const router = useRouter()
const list = ref([])
const sort = ref('time')
const loading = ref(false)

const hotTopics = ['稻瘟病防治', '玉米锈病', '棚室降湿', '科学用药', '有机种植']
const activeUsers = SEED_ACTIVE_USERS

function formatTime(t) {
  if (!t) return '—'
  return String(t).replace('T', ' ').slice(0, 16)
}

function goCreate() {
  router.push('/user/community/post')
}

function onTopic(topic) {
  ElMessage.info(`话题「${topic}」筛选即将上线`)
}

function mapPost(p) {
  const imagesField = Array.isArray(p.images) ? p.images[0] : p.images
  const { category, urls } = unpackPostImages(imagesField)
  const tags = []
  if (p.tag === 'GUIDE' || p.pinned) {
    tags.push({ label: '官方', type: 'success' })
  }
  if (p.authorRole === 'EXPERT') {
    tags.push({ label: '专家', type: 'warning' })
  }
  if (category === 'help') {
    tags.push({ label: '求助', type: 'warning' })
  } else if (category === 'experience') {
    tags.push({ label: '经验', type: 'success' })
  } else if (p.tag !== 'GUIDE') {
    tags.push({ label: categoryLabel(category), type: 'info' })
  }
  const raw = (p.content || '').replace(/^\s*<!--\w+-->\s*/, '')
  return {
    ...p,
    cover: urls[0] || '',
    summary: raw.slice(0, 72) || '点击查看完整内容',
    authorName: p.authorName || p.nickname || `农友${p.authorId || ''}`,
    displayTags: tags
  }
}

function seedFallback() {
  const rows = [...SEED_POSTS]
  if (sort.value === 'hot') {
    rows.sort((a, b) => (b.viewCount || 0) - (a.viewCount || 0))
  } else {
    rows.sort((a, b) => String(b.createTime).localeCompare(String(a.createTime)))
  }
  return rows.map(mapPost)
}

async function load() {
  loading.value = true
  try {
    const res = await postList({ page: 1, size: 20, sort: sort.value })
    const records = res.data?.records || []
    list.value = records.length ? records.map(mapPost) : seedFallback()
  } catch {
    list.value = seedFallback()
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.community-page {
  max-width: 1100px;
  margin: 0 auto;
  padding-bottom: 40px;
}

.layout {
  display: flex;
  align-items: flex-start;
  gap: 24px;
}

.main-col {
  flex: 0 0 70%;
  max-width: 70%;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.side-col {
  flex: 0 0 calc(30% - 24px);
  max-width: calc(30% - 24px);
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
  position: sticky;
  top: 88px;
}

.card-soft {
  background: #fff;
  border: none;
  border-radius: 16px;
  box-shadow: 0 6px 20px rgba(15, 23, 42, 0.05);
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
}

.sort-tabs :deep(.el-radio-button__inner) {
  border: none !important;
  background: #f3f4f6;
  color: #6b7280;
  border-radius: 999px !important;
  padding: 8px 16px;
  box-shadow: none !important;
  font-weight: 600;
}
.sort-tabs :deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-radius: 999px !important;
}
.sort-tabs :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 999px !important;
}
.sort-tabs :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: #eaf8ee !important;
  color: #1f8f36 !important;
  box-shadow: none !important;
}
.sort-tabs :deep(.el-radio-button + .el-radio-button) {
  margin-left: 8px;
}

.btn-post {
  --el-button-bg-color: #28b446;
  --el-button-border-color: #28b446;
  --el-button-hover-bg-color: #1f8f36;
  --el-button-hover-border-color: #1f8f36;
  font-weight: 700;
  padding: 10px 22px;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.post-card {
  display: flex;
  gap: 16px;
  padding: 16px;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}
.post-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(40, 180, 70, 0.12);
}
.post-card.skeleton {
  cursor: default;
  padding: 20px;
}
.post-card.skeleton:hover {
  transform: none;
  box-shadow: 0 6px 20px rgba(15, 23, 42, 0.05);
}

.cover {
  width: 120px;
  height: 96px;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;
  background: #eef2f0;
}
.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: center;
}
.title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  flex-wrap: wrap;
}
.title-row h3 {
  margin: 0;
  font-size: 1.05rem;
  color: #163b1f;
  font-weight: 750;
  line-height: 1.35;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 120px;
}
.tag :deep(.el-tag) {
  border: none;
}
.summary {
  margin: 0;
  color: #6b7280;
  font-size: 0.88rem;
  line-height: 1.55;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  font-size: 0.78rem;
  color: #9ca3af;
}
.author {
  color: #28b446;
  font-weight: 650;
}
.dot {
  opacity: 0.6;
}

.empty-panel {
  padding: 48px 20px;
  min-height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.side-card {
  padding: 18px 16px;
}
.side-card h4 {
  margin: 0 0 14px;
  font-size: 0.98rem;
  color: #163b1f;
  font-weight: 750;
}

.topics {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.topic-tag {
  border: none;
  background: #f0faf3;
  color: #1f8f36;
  border-radius: 999px;
  padding: 7px 12px;
  font-size: 0.82rem;
  font-weight: 650;
  cursor: pointer;
  transition: 0.15s ease;
}
.topic-tag:hover {
  background: #dcf5e3;
}

.users {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.users li {
  display: flex;
  align-items: center;
  gap: 10px;
}
.avatar {
  background: #eaf8ee;
  color: #1f8f36;
  font-weight: 700;
}
.u-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.u-meta strong {
  font-size: 0.9rem;
  color: #1f2937;
}
.u-meta span {
  font-size: 0.75rem;
  color: #9ca3af;
}

@media (max-width: 900px) {
  .layout {
    flex-direction: column;
  }
  .main-col,
  .side-col {
    flex: none;
    max-width: none;
    width: 100%;
  }
  .side-col {
    position: static;
  }
}
</style>
