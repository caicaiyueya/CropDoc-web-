<template>
  <div class="ai-page">
    <header class="page-title">
      <h1>AI 智能问诊</h1>
      <p>自动诊断与自由问答 · 基于智谱 GLM-5</p>
    </header>

    <div class="tri-layout">
      <!-- 左：会话历史 -->
      <aside class="col col-left card">
        <el-button type="primary" class="btn-new" :icon="Plus" @click="createSession">
          新建对话
        </el-button>
        <div class="session-list">
          <button
            v-for="s in sessions"
            :key="s.sessionId"
            type="button"
            class="session-item"
            :class="{ active: s.sessionId === activeSessionId }"
            @click="switchSession(s.sessionId)"
          >
            <span class="session-title">{{ s.title }}</span>
            <span class="session-time">{{ formatTime(s.updatedAt) }}</span>
          </button>
          <el-empty v-if="!sessions.length" description="暂无会话" :image-size="56" />
        </div>
      </aside>

      <!-- 中：聊天窗口 -->
      <section class="col col-center card">
        <div class="chat-head">
          <img class="ai-logo" src="/images/logo.png" alt="AI" />
          <div>
            <strong>云农智诊助手</strong>
            <p>{{ chatSubtitle }}</p>
          </div>
        </div>

        <div ref="listRef" class="chat-list">
          <div v-if="!messages.length && !loading" class="welcome">
            <p>你好，我是云农智诊智能助手。</p>
            <p>可从识别页跳转自动生成防治建议，或直接输入农技问题。</p>
          </div>

          <div
            v-for="(m, i) in messages"
            :key="i"
            class="msg"
            :class="m.role === 'user' ? 'user' : 'ai'"
          >
            <img v-if="m.role !== 'user'" class="avatar" src="/images/logo.png" alt="" />
            <!-- 用户消息纯文本；AI 消息 Markdown 安全渲染 -->
            <div
              v-if="m.role === 'user'"
              class="bubble"
            >{{ m.content }}</div>
            <div
              v-else
              class="bubble markdown-body"
              v-html="renderMarkdown(m.content)"
            />
          </div>

          <div v-if="loading" class="msg ai thinking">
            <img class="avatar" src="/images/logo.png" alt="" />
            <div class="bubble skeleton-bubble">
              <p class="thinking-label">AI正在思考中...</p>
              <el-skeleton animated :rows="3" />
            </div>
          </div>
        </div>

        <div class="composer">
          <el-input
            v-model="question"
            type="textarea"
            :rows="2"
            resize="none"
            placeholder="输入问题，例如：玉米叶子发黄怎么办"
            :disabled="loading"
            @keydown.enter.exact.prevent="send()"
          />
          <el-button type="primary" class="send" :loading="loading" @click="send()">
            发送
          </el-button>
        </div>
      </section>

      <!-- 右：上下文面板 -->
      <aside class="col col-right card">
        <template v-if="hasDetectMeta">
          <h3>智能诊断结果</h3>
          <p class="side-sub">{{ detectMeta.result }}</p>
          <div v-if="detectMeta.imageUrl" class="thumb-wrap">
            <img :src="detectMeta.imageUrl" alt="识别图片" />
          </div>
          <el-collapse v-model="activePanels" class="accordion">
            <el-collapse-item title="发病原因" name="cause">
              <p>{{ panels.cause }}</p>
            </el-collapse-item>
            <el-collapse-item title="发病条件" name="condition">
              <p>{{ panels.condition }}</p>
            </el-collapse-item>
            <el-collapse-item title="防治措施" name="treat">
              <p>{{ panels.treat }}</p>
            </el-collapse-item>
            <el-collapse-item title="识别摘要" name="summary">
              <p v-if="detectMeta.confidence != null">
                置信度：{{ formatConfidence(detectMeta.confidence) }}
              </p>
              <p v-if="detectMeta.crop">作物：{{ detectMeta.crop }}</p>
              <p v-else class="muted">可在左侧新建对话或继续追问用药细节。</p>
            </el-collapse-item>
          </el-collapse>
        </template>

        <template v-else>
          <h3>猜你想问</h3>
          <p class="side-sub">点击快捷提问，开启自由问答</p>
          <div class="suggest-tags">
            <button
              v-for="q in SUGGEST_QUESTIONS"
              :key="q"
              type="button"
              class="suggest-tag"
              :disabled="loading"
              @click="send(q)"
            >
              {{ q }}
            </button>
          </div>
        </template>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { diagnosisChat } from '@/api/detection'
import { useHistoryStore } from '@/store/history'
import { getCauseTips, getPreventionTips } from '@/utils/diseaseMap'
import { renderMarkdown } from '@/utils/markdown'

const STORAGE_KEY = 'ynzz_ai_chat_sessions_v1'
const SUGGEST_QUESTIONS = [
  '玉米叶子发黄怎么办？',
  '常见叶斑病如何防治？',
  '打药需要注意哪些安全间隔？',
  '雨后田间病害如何紧急处理？',
  '如何区分缺素黄化和病害黄化？'
]

const route = useRoute()
const historyStore = useHistoryStore()

const question = ref('')
const loading = ref(false)
const sessions = ref([])
const activeSessionId = ref('')
const messages = ref([])
const listRef = ref(null)
const activePanels = ref(['cause', 'treat'])
const autoAsked = ref(false)
/** 当前自动诊断会话是否已写入全局诊断历史 */
const autoHistorySaved = ref(false)

const detectMeta = reactive({
  result: '',
  confidence: null,
  crop: '',
  imageUrl: ''
})

const hasDetectMeta = computed(() => !!detectMeta.result)

const chatSubtitle = computed(() =>
  hasDetectMeta.value
    ? `已关联识别结果「${detectMeta.result}」`
    : '自由农技问答，无需上传图片'
)

const queryContext = computed(() => {
  const disease = (route.query.disease || '').toString().trim()
  if (!disease) return null
  const confRaw = route.query.confidence
  let confidence = null
  if (confRaw != null && confRaw !== '') {
    const n = Number(confRaw)
    if (!Number.isNaN(n)) confidence = n
  }
  return {
    disease,
    confidence,
    crop: (route.query.crop || '').toString().trim(),
    imageUrl: (route.query.image_url || '').toString().trim()
  }
})

const contextHint = computed(() => {
  if (!detectMeta.result) return ''
  const parts = [`识别结果=${detectMeta.result}`]
  if (detectMeta.confidence != null) parts.push(`置信度=${detectMeta.confidence}`)
  if (detectMeta.crop) parts.push(`作物=${detectMeta.crop}`)
  return parts.join('; ')
})

const panels = computed(() => {
  const name = detectMeta.result || '该病害'
  return {
    cause: getCauseTips(name),
    condition:
      '高温高湿、通风不良、叶面结露或偏施氮肥时更易流行；连作、密植田块菌源积累快，风险更高。',
    treat: getPreventionTips(name)
  }
})

function uid() {
  return `s-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
}

function loadSessions() {
  try {
    const raw = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
    sessions.value = Array.isArray(raw) ? raw : []
  } catch {
    sessions.value = []
  }
}

function persistSessions() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(sessions.value))
}

function currentSession() {
  return sessions.value.find((s) => s.sessionId === activeSessionId.value) || null
}

function syncActiveToStore() {
  const s = currentSession()
  if (!s) return
  s.messages = messages.value.map((m) => ({ role: m.role, content: m.content }))
  s.updatedAt = new Date().toISOString()
  s.meta = {
    disease: detectMeta.result || '',
    confidence: detectMeta.confidence,
    crop: detectMeta.crop || '',
    imageUrl: detectMeta.imageUrl || ''
  }
  if (messages.value.length) {
    const firstUser = messages.value.find((m) => m.role === 'user')
    if (firstUser) {
      s.title = firstUser.content.slice(0, 28) + (firstUser.content.length > 28 ? '…' : '')
    }
  } else if (detectMeta.result) {
    s.title = `诊断：${detectMeta.result}`
  }
  sessions.value = [...sessions.value]
  persistSessions()
}

function applyMeta(meta = {}) {
  detectMeta.result = meta.disease || ''
  detectMeta.confidence = meta.confidence ?? null
  detectMeta.crop = meta.crop || ''
  detectMeta.imageUrl = meta.imageUrl || ''
}

function createSession(opts = {}) {
  const { meta = {}, activate = true, title } = opts
  const session = {
    sessionId: uid(),
    title: title || (meta.disease ? `诊断：${meta.disease}` : '新对话'),
    updatedAt: new Date().toISOString(),
    messages: [],
    meta: {
      disease: meta.disease || '',
      confidence: meta.confidence ?? null,
      crop: meta.crop || '',
      imageUrl: meta.imageUrl || ''
    }
  }
  sessions.value = [session, ...sessions.value]
  persistSessions()
  if (activate) {
    activeSessionId.value = session.sessionId
    messages.value = []
    applyMeta(session.meta)
    autoAsked.value = false
  }
  return session
}

function switchSession(sessionId) {
  if (loading.value) return
  const s = sessions.value.find((x) => x.sessionId === sessionId)
  if (!s) return
  syncActiveToStore()
  activeSessionId.value = sessionId
  messages.value = (s.messages || []).map((m) => ({ ...m }))
  applyMeta(s.meta || {})
  scrollBottom()
}

function formatTime(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return ''
  const pad = (n) => String(n).padStart(2, '0')
  return `${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function formatConfidence(c) {
  const n = Number(c)
  if (Number.isNaN(n)) return '--'
  return `${(n <= 1 ? n * 100 : n).toFixed(1)}%`
}

async function scrollBottom() {
  await nextTick()
  if (listRef.value) listRef.value.scrollTop = listRef.value.scrollHeight
}

/**
 * @returns {Promise<{ ok: boolean, answer?: string }>}
 */
async function send(preset) {
  const q = (typeof preset === 'string' ? preset : question.value).trim()
  if (!q || loading.value) return { ok: false }

  if (!activeSessionId.value) {
    createSession({
      meta: {
        disease: detectMeta.result,
        confidence: detectMeta.confidence,
        crop: detectMeta.crop,
        imageUrl: detectMeta.imageUrl
      }
    })
  }

  messages.value.push({ role: 'user', content: q })
  if (typeof preset !== 'string') question.value = ''
  loading.value = true
  syncActiveToStore()
  await scrollBottom()

  try {
    const history = messages.value
      .slice(0, -1)
      .filter((m) => m.role === 'user' || m.role === 'assistant')
      .map((m) => ({ role: m.role, content: m.content }))

    const payload = {
      sessionId: activeSessionId.value || null,
      question: q,
      contextHint: contextHint.value || undefined,
      history: history.slice(-12)
    }

    const res = await diagnosisChat(payload)
    const rawAnswer = res?.data?.answer ?? res?.answer
    const answer =
      rawAnswer != null && String(rawAnswer).trim() && String(rawAnswer).trim().toLowerCase() !== 'null'
        ? String(rawAnswer).trim()
        : '暂无回复，请稍后重试'
    if (res?.data?.sessionId) {
      const oldId = activeSessionId.value
      const s = currentSession()
      if (s && res.data.sessionId !== oldId) {
        s.sessionId = res.data.sessionId
        activeSessionId.value = res.data.sessionId
      }
    }
    messages.value.push({ role: 'assistant', content: answer })
    syncActiveToStore()
    await scrollBottom()
    return { ok: true, answer }
  } catch (e) {
    messages.value.pop()
    syncActiveToStore()
    // request 拦截器已弹过业务错误；仅网络类补一条
    if (!e?.message && !e?.code) {
      ElMessage.error('问诊失败，请稍后重试')
    }
    return { ok: false }
  } finally {
    loading.value = false
  }
}

function saveAutoConsultHistory(answer) {
  if (autoHistorySaved.value) return
  const disease = detectMeta.result || queryContext.value?.disease || ''
  if (!disease) return
  const summary =
    typeof answer === 'string' && answer.trim()
      ? answer.trim().slice(0, 120) + (answer.trim().length > 120 ? '…' : '')
      : `针对「${disease}」的自动防治建议`
  historyStore.addRecord(
    {
      type: 'AI_CONSULT',
      crop: detectMeta.crop || queryContext.value?.crop || '',
      diseaseName: disease,
      confidence: detectMeta.confidence ?? queryContext.value?.confidence ?? 0,
      imageUrl: detectMeta.imageUrl || queryContext.value?.imageUrl || '',
      summary
    },
    { dedupeDiseaseDay: true }
  )
  autoHistorySaved.value = true
}

async function autoDiagnoseFromQuery() {
  const q = queryContext.value
  if (!q || autoAsked.value) return
  autoAsked.value = true
  autoHistorySaved.value = false

  applyMeta({
    disease: q.disease,
    confidence: q.confidence,
    crop: q.crop,
    imageUrl: q.imageUrl
  })

  const existing = sessions.value.find(
    (s) => s.meta?.disease === q.disease && (!s.messages || !s.messages.length)
  )
  if (existing) {
    activeSessionId.value = existing.sessionId
    messages.value = []
    applyMeta(existing.meta)
  } else {
    createSession({
      meta: {
        disease: q.disease,
        confidence: q.confidence,
        crop: q.crop,
        imageUrl: q.imageUrl
      },
      title: `诊断：${q.disease}`
    })
  }

  const autoQ = `请为[${q.disease}]生成防治建议`
  const { ok, answer } = await send(autoQ)
  if (ok) saveAutoConsultHistory(answer)
}

onMounted(async () => {
  loadSessions()
  if (queryContext.value) {
    await autoDiagnoseFromQuery()
    return
  }
  if (sessions.value.length) {
    switchSession(sessions.value[0].sessionId)
  } else {
    createSession()
  }
})

watch(
  () => route.query.disease,
  async (disease, prev) => {
    if (disease && disease !== prev) {
      autoAsked.value = false
      await autoDiagnoseFromQuery()
    }
  }
)
</script>

<style scoped>
.ai-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: calc(100vh - 140px);
  padding-bottom: 24px;
}
.page-title h1 {
  margin: 0 0 6px;
  font-size: 1.5rem;
  color: #163b1f;
}
.page-title p {
  margin: 0;
  color: #6b7280;
  font-size: 0.9rem;
}

.tri-layout {
  display: flex;
  align-items: stretch;
  gap: 16px;
  flex: 1;
  min-height: 620px;
}
.card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.05);
}
.col {
  display: flex;
  flex-direction: column;
  min-height: 620px;
  box-sizing: border-box;
}
.col-left {
  flex: 0 0 22%;
  max-width: 22%;
  padding: 16px;
  min-width: 0;
}
.col-center {
  flex: 1 1 48%;
  min-width: 0;
  overflow: hidden;
}
.col-right {
  flex: 0 0 26%;
  max-width: 26%;
  padding: 18px 16px;
  min-width: 0;
}

.btn-new {
  width: 100%;
  margin-bottom: 14px;
  --el-button-bg-color: #28b446;
  --el-button-border-color: #28b446;
  --el-button-hover-bg-color: #1f8f36;
  --el-button-hover-border-color: #1f8f36;
}
.session-list {
  flex: 1;
  overflow: auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.session-item {
  text-align: left;
  border: 1px solid #eef2f0;
  background: #fafbfa;
  border-radius: 12px;
  padding: 10px 12px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 4px;
  transition: 0.15s ease;
}
.session-item:hover {
  border-color: #86d89a;
}
.session-item.active {
  background: #eaf8ee;
  border-color: #28b446;
}
.session-title {
  font-size: 0.88rem;
  font-weight: 650;
  color: #1f2937;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.session-time {
  font-size: 0.72rem;
  color: #9ca3af;
}

.chat-head {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid #eef2f0;
  flex-shrink: 0;
}
.ai-logo,
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  object-fit: cover;
  flex-shrink: 0;
}
.chat-head strong {
  display: block;
  color: #163b1f;
}
.chat-head p {
  margin: 2px 0 0;
  color: #9ca3af;
  font-size: 0.8rem;
}

.chat-list {
  flex: 1;
  overflow: auto;
  padding: 18px 16px;
  background: linear-gradient(180deg, #fbfcfb, #f7f8fa);
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 0;
}
.welcome {
  margin: auto 0;
  text-align: center;
  color: #9ca3af;
  font-size: 0.9rem;
  line-height: 1.7;
}
.msg {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  max-width: 85%;
  min-width: 0;
}
.msg.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}
.bubble {
  padding: 12px 14px;
  border-radius: 16px;
  line-height: 1.6;
  font-size: 0.94rem;
  word-break: break-word;
  overflow-wrap: anywhere;
  min-width: 0;
  max-width: 100%;
  box-sizing: border-box;
}
.msg.ai .bubble {
  background: #fff;
  color: #1f2937;
  border: 1px solid #eef2f0;
  border-top-left-radius: 6px;
}
.msg.user .bubble {
  background: #28b446;
  color: #fff;
  border-top-right-radius: 6px;
  box-shadow: 0 8px 18px rgba(40, 180, 70, 0.2);
  white-space: pre-wrap;
}

.msg.ai .markdown-body {
  white-space: normal;
}
.skeleton-bubble {
  min-width: 220px;
}
.thinking-label {
  margin: 0 0 10px;
  font-size: 0.82rem;
  color: #6b7280;
  font-weight: 600;
}

.composer {
  display: grid;
  grid-template-columns: 1fr 96px;
  gap: 10px;
  align-items: end;
  padding: 14px 16px;
  border-top: 1px solid #eef2f0;
  background: #fff;
  flex-shrink: 0;
}
.send {
  height: 44px;
  --el-button-bg-color: #28b446;
  --el-button-border-color: #28b446;
}

.col-right h3 {
  margin: 0 0 6px;
  color: #163b1f;
  font-size: 1.05rem;
}
.side-sub {
  margin: 0 0 14px;
  color: #9ca3af;
  font-size: 0.82rem;
}
.thumb-wrap {
  margin-bottom: 12px;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #eef2f0;
  max-height: 140px;
}
.thumb-wrap img {
  width: 100%;
  height: 140px;
  object-fit: cover;
  display: block;
}
.accordion :deep(.el-collapse-item__header) {
  font-weight: 700;
  color: #1f2937;
}
.accordion :deep(.el-collapse-item__content) {
  color: #4b5563;
  line-height: 1.7;
  font-size: 0.88rem;
  padding-bottom: 12px;
}
.muted {
  color: #9ca3af;
}

.suggest-tags {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.suggest-tag {
  text-align: left;
  border: 1px solid #e5e7eb;
  background: #f8faf9;
  border-radius: 12px;
  padding: 12px 14px;
  font-size: 0.88rem;
  color: #374151;
  cursor: pointer;
  transition: 0.15s ease;
}
.suggest-tag:hover:not(:disabled) {
  border-color: #28b446;
  color: #1f8f36;
  background: #eaf8ee;
}
.suggest-tag:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

@media (max-width: 1100px) {
  .tri-layout {
    flex-direction: column;
  }
  .col-left,
  .col-center,
  .col-right {
    flex: none;
    max-width: none;
    width: 100%;
    min-height: auto;
  }
  .col-center {
    min-height: 480px;
  }
  .session-list {
    max-height: 180px;
  }
}
</style>

<!-- v-html 内容不受 scoped 影响，单独提供 Markdown 排版 -->
<style>
.ai-page .markdown-body > *:first-child {
  margin-top: 0 !important;
}
.ai-page .markdown-body > *:last-child {
  margin-bottom: 0 !important;
}
.ai-page .markdown-body h1,
.ai-page .markdown-body h2,
.ai-page .markdown-body h3,
.ai-page .markdown-body h4 {
  margin: 14px 0 10px;
  color: #163b1f;
  font-weight: 750;
  line-height: 1.35;
}
.ai-page .markdown-body h1 { font-size: 1.2rem; }
.ai-page .markdown-body h2 { font-size: 1.1rem; }
.ai-page .markdown-body h3 { font-size: 1.02rem; }
.ai-page .markdown-body h4 { font-size: 0.96rem; }
.ai-page .markdown-body p {
  margin: 0 0 12px;
  line-height: 1.6;
  color: #1f2937;
}
.ai-page .markdown-body strong,
.ai-page .markdown-body b {
  font-weight: 700;
  color: #14351c;
}
.ai-page .markdown-body em,
.ai-page .markdown-body i {
  font-style: italic;
}
.ai-page .markdown-body ul,
.ai-page .markdown-body ol {
  margin: 0 0 12px;
  padding-left: 1.35em;
  line-height: 1.6;
}
.ai-page .markdown-body li {
  margin-bottom: 6px;
}
.ai-page .markdown-body li > p {
  margin-bottom: 6px;
}
.ai-page .markdown-body ul { list-style: disc; }
.ai-page .markdown-body ol { list-style: decimal; }
.ai-page .markdown-body blockquote {
  margin: 0 0 12px;
  padding: 8px 12px;
  border-left: 3px solid #86d89a;
  background: #f6faf7;
  color: #4b5563;
  border-radius: 0 8px 8px 0;
}
.ai-page .markdown-body a {
  color: #1f8f36;
  text-decoration: underline;
  word-break: break-all;
}
.ai-page .markdown-body hr {
  border: none;
  border-top: 1px solid #e5e7eb;
  margin: 14px 0;
}
.ai-page .markdown-body code {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 0.86em;
  background: #f3f4f6;
  padding: 0.12em 0.4em;
  border-radius: 4px;
  word-break: break-word;
}
.ai-page .markdown-body pre {
  margin: 0 0 12px;
  padding: 12px 14px;
  background: #f3f4f6;
  border-radius: 10px;
  overflow-x: auto;
  max-width: 100%;
}
.ai-page .markdown-body pre code {
  background: transparent;
  padding: 0;
  font-size: 0.84rem;
  white-space: pre-wrap;
  word-break: break-word;
}
.ai-page .markdown-body table {
  display: block;
  width: 100%;
  max-width: 100%;
  overflow-x: auto;
  border-collapse: collapse;
  margin: 0 0 12px;
  font-size: 0.88rem;
}
.ai-page .markdown-body th,
.ai-page .markdown-body td {
  border: 1px solid #e5e7eb;
  padding: 8px 10px;
  text-align: left;
  word-break: break-word;
}
.ai-page .markdown-body th {
  background: #f8faf9;
  font-weight: 700;
  color: #163b1f;
}
</style>
