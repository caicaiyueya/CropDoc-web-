<template>
  <div class="page">
    <h2>智能诊断</h2>
    <div class="chat">
      <div v-for="(m, i) in messages" :key="i" class="msg" :class="m.role">
        <strong>{{ m.role === 'user' ? '我' : '助手' }}：</strong>{{ m.content }}
      </div>
    </div>
    <div class="composer">
      <el-input v-model="question" type="textarea" :rows="2" placeholder="例如：怎么防治？需要打几次药？" />
      <el-button type="primary" :loading="loading" @click="send">发送</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { diagnosisChat } from '@/api/detection'

const route = useRoute()
const question = ref('')
const loading = ref(false)
const sessionId = ref('')
const messages = ref([])

async function send() {
  if (!question.value.trim()) return
  const q = question.value.trim()
  messages.value.push({ role: 'user', content: q })
  question.value = ''
  loading.value = true
  try {
    const res = await diagnosisChat({
      detectionRecordId: Number(route.params.recordId),
      sessionId: sessionId.value || null,
      question: q
    })
    sessionId.value = res.data.sessionId
    messages.value.push({ role: 'assistant', content: res.data.answer })
  } catch (e) {
    messages.value.pop()
    ElMessage.error('诊断失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
h2 { color: #1b4332; }
.chat {
  min-height: 280px;
  background: #fff;
  border: 1px solid #e0ebe3;
  border-radius: 12px;
  padding: 0.85rem;
  margin-bottom: 0.75rem;
}
.msg { margin-bottom: 0.75rem; line-height: 1.5; font-size: 0.92rem; }
.msg.user { color: #1b4332; }
.msg.assistant { color: #2d6a4f; }
.composer { display: grid; gap: 0.5rem; }
</style>
