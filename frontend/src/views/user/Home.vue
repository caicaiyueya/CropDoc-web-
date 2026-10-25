<template>
  <div class="home">
    <section class="hero card">
      <div class="hero-left">
        <p class="eyebrow">YunNong ZhiZhen · 云农智诊</p>
        <h1>AI助力智慧农业</h1>
        <p class="subtitle">
          拍照识病、智能问诊、农友交流一站完成。用视觉模型与农技知识，帮助农户快速研判病虫害并获取防治建议。
        </p>
        <button class="btn-primary" type="button" @click="$router.push('/user/detect')">
          立即拍照识病
        </button>
      </div>
      <div class="hero-right">
        <img src="/images/hero-farm.jpg" alt="高清农田" />
      </div>
    </section>

    <section class="content-grid">
      <div class="panel card">
        <div class="panel-head">
          <h2>常见病害科普</h2>
          <router-link to="/user/disease" class="more-link">查看病害库 →</router-link>
        </div>
        <div class="disease-grid">
          <article
            v-for="d in diseases"
            :key="d.id || d.title"
            class="disease-card"
            @click="$router.push(d.id ? `/user/disease` : '/user/disease')"
          >
            <div class="thumb-wrap">
              <img
                class="thumb"
                :src="d.cover || DISEASE_PLACEHOLDER"
                :alt="d.title"
                @error="onDiseaseImgError"
              />
            </div>
            <div class="meta">
              <h4>{{ d.title }}</h4>
              <p>{{ d.desc }}</p>
            </div>
          </article>
        </div>
      </div>

      <div class="panel card">
        <div class="panel-head">
          <h2>今日农业资讯</h2>
          <router-link to="/user/news" class="more-link">更多 →</router-link>
        </div>
        <div class="news-list">
          <article
            v-for="n in news"
            :key="n.id || n.title"
            class="news-item"
            @click="n.id ? $router.push(`/user/news/${n.id}`) : $router.push('/user/news')"
          >
            <div class="news-thumb" aria-hidden="true">
              <img
                v-if="n.cover"
                :src="n.cover"
                :alt="n.title"
                class="news-cover"
              />
              <svg v-else class="cal-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6">
                <rect x="3.5" y="5" width="17" height="15.5" rx="2.5" />
                <path d="M8 3.5v3M16 3.5v3M3.5 10h17" />
                <circle cx="8.5" cy="14" r="1" fill="currentColor" stroke="none" />
                <circle cx="12" cy="14" r="1" fill="currentColor" stroke="none" />
                <circle cx="15.5" cy="14" r="1" fill="currentColor" stroke="none" />
              </svg>
            </div>
            <div class="news-body">
              <h4>{{ n.title }}</h4>
              <time>{{ formatTime(n.createTime) }}</time>
            </div>
          </article>
          <el-empty v-if="!news.length" description="暂无资讯" :image-size="64" />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { diseaseList, newsList } from '@/api/content'
import { resolveNewsCoverByTitle } from '@/utils/newsMock'
import { resolveStandardDiseaseImage } from '@/utils/diseaseCatalog'

const DISEASE_PLACEHOLDER = '/images/hero-farm.jpg'
const POS = ['18% 40%', '72% 35%', '45% 70%', '30% 20%']

const news = ref([])
const diseases = ref([])

function formatTime(t) {
  if (!t) return '今日'
  return String(t).replace('T', ' ').slice(0, 16)
}

function onDiseaseImgError(e) {
  const el = e?.target
  if (el && el.src && !el.src.includes('hero-farm')) {
    el.src = DISEASE_PLACEHOLDER
  }
}

function mapDiseases(apiList) {
  return (apiList || []).slice(0, 3).map((d, i) => ({
    id: d.id,
    title: d.name,
    desc: String(d.description || '点击查看症状特征与防治建议')
      .replace(/【[^】]+】/g, '')
      .trim()
      .slice(0, 48),
    cover:
      d.image_url ||
      d.imageUrl ||
      d.cover ||
      resolveStandardDiseaseImage({ name: d.name }) ||
      DISEASE_PLACEHOLDER,
    pos: POS[i % POS.length]
  }))
}

onMounted(async () => {
  try {
    const [nRes, dRes] = await Promise.all([
      newsList({ page: 1, size: 5 }),
      diseaseList({ page: 1, size: 6 })
    ])
    const apiNews = nRes.data?.records || []
    news.value = apiNews.map((n, i) => ({
      ...n,
      cover: n.cover || resolveNewsCoverByTitle(n.title) || null,
      _pos: POS[i % POS.length]
    }))
    diseases.value = mapDiseases(dRes.data?.records || [])
  } catch {
    news.value = []
    diseases.value = []
  }
})
</script>

<style scoped>
/* 普通页：文档流排版，滚动由 Layout 的 .page-main 承担 */
.home {
  display: flex;
  flex-direction: column;
  gap: 48px;
  padding-bottom: 8px;
}
.card {
  background: var(--card);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
}
.hero {
  display: grid;
  grid-template-columns: 40% 60%;
  overflow: hidden;
  min-height: 380px;
}
.hero-left {
  padding: 48px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.eyebrow {
  margin: 0 0 12px;
  color: var(--brand);
  font-weight: 700;
  font-size: 0.85rem;
  letter-spacing: 0.06em;
}
.hero-left h1 {
  margin: 0;
  font-size: clamp(2rem, 3vw, 2.75rem);
  line-height: 1.2;
  color: #14351c;
}
.subtitle {
  margin: 16px 0 28px;
  color: var(--muted);
  line-height: 1.7;
  font-size: 1rem;
}
.btn-primary {
  align-self: flex-start;
  border: none;
  background: var(--brand);
  color: #fff;
  font-size: 1rem;
  font-weight: 700;
  padding: 14px 28px;
  border-radius: 999px;
  cursor: pointer;
  box-shadow: 0 10px 24px rgba(40, 180, 70, 0.28);
  transition: transform 0.15s ease, background 0.15s ease;
}
.btn-primary:hover {
  background: var(--brand-dark);
  transform: translateY(-1px);
}
.hero-right { position: relative; min-height: 320px; }
.hero-right img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.content-grid {
  display: grid;
  grid-template-columns: 58% 42%;
  gap: 20px;
  align-items: stretch;
}
.panel {
  padding: 22px 24px 20px;
  display: flex;
  flex-direction: column;
  height: 100%;
}
.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  flex-shrink: 0;
}
.panel-head h2 {
  margin: 0;
  font-size: 1.15rem;
  color: #163b1f;
  font-weight: 700;
}

/* 次级灰绿色链接 + Hover 加深/下划线 */
.more-link {
  color: #6f9e7c;
  font-size: 0.88rem;
  font-weight: 600;
  text-decoration: none;
  transition: color 0.18s ease, text-decoration-color 0.18s ease;
  text-underline-offset: 3px;
}
.more-link:hover {
  color: #1f8f36;
  text-decoration: underline;
  text-decoration-color: #28b446;
}

/* 上下三张横向图文卡：左侧固定尺寸封面，与资讯缩略图视觉对齐 */
.disease-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
}
.disease-card {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 12px;
  border: 1px solid #eef2f0;
  border-radius: 14px;
  cursor: pointer;
  background: #fff;
  flex: 1;
  transition: border-color 0.15s ease, box-shadow 0.15s ease, transform 0.15s ease;
}
.disease-card:hover {
  border-color: #c5e8cd;
  box-shadow: 0 8px 20px rgba(40, 180, 70, 0.1);
  transform: translateY(-1px);
}
.thumb-wrap {
  width: 140px;
  height: 100px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
  background: #e8f5eb;
}
.thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  display: block;
}
.meta {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 2px 4px 2px 0;
  align-self: stretch;
}
.meta h4 {
  margin: 0 0 8px;
  font-size: 1rem;
  color: #1f2937;
  font-weight: 700;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.meta p {
  margin: 0;
  color: #6b7280;
  font-size: 0.84rem;
  line-height: 1.55;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.news-item {
  display: flex;
  gap: 12px;
  padding: 10px 8px;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.15s ease;
}
.news-item:hover { background: #f4faf6; }
.news-thumb {
  width: 100px;
  height: 70px;
  border-radius: 8px;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
  color: #4b5563;
  overflow: hidden;
}
.news-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.cal-icon {
  width: 28px;
  height: 28px;
}
.news-body {
  flex: 1;
  min-width: 0;
}
.news-body h4 {
  margin: 2px 0 6px;
  font-size: 0.92rem;
  line-height: 1.45;
  color: #1f2937;
  font-weight: 600;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.news-body time {
  color: #9ca3af;
  font-size: 0.78rem;
}

@media (max-width: 960px) {
  .home { gap: 32px; }
  .hero { grid-template-columns: 1fr; }
  .hero-right { min-height: 220px; }
  .hero-left { padding: 28px 22px; }
  .content-grid { grid-template-columns: 1fr; }
}
</style>
