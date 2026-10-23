<template>
  <div class="page" v-loading="loading">
    <template v-if="post">
      <h2>{{ post.title }}</h2>
      <div class="meta">
        <el-tag v-if="post.tag === 'GUIDE'" size="small" type="success">农技指导</el-tag>
        <span>{{ post.createTime }}</span>
      </div>
      <p class="content">{{ post.content }}</p>
      <div class="ops">
        <el-button @click="onFav">收藏</el-button>
        <el-button @click="onReport">举报</el-button>
      </div>
      <h3>评论</h3>
      <div v-for="c in comments" :key="c.id" class="comment">
        <div class="c-head">
          <el-tag v-if="c.isProfessional" size="small" type="warning">专业回答</el-tag>
          <el-tag v-if="c.isQuality" size="small" type="success">优质</el-tag>
          <el-button v-if="store.isExpert" link type="primary" @click="onMark(c.id)">标优质</el-button>
        </div>
        <p>{{ c.content }}</p>
      </div>
      <el-input v-model="commentText" type="textarea" :rows="2" placeholder="写下你的看法" />
      <el-button type="primary" class="mt" @click="onComment">发表评论</el-button>
    </template>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createComment, createReport, markQuality, postDetail, toggleFavorite } from '@/api/post'
import { useUserStore } from '@/store/user'

const route = useRoute()
const store = useUserStore()
const loading = ref(false)
const post = ref(null)
const comments = ref([])
const commentText = ref('')

async function load() {
  loading.value = true
  try {
    const res = await postDetail(route.params.id)
    post.value = res.data.post
    comments.value = res.data.comments || []
  } finally {
    loading.value = false
  }
}

async function onComment() {
  if (!commentText.value.trim()) return
  await createComment({ postId: Number(route.params.id), content: commentText.value.trim() })
  commentText.value = ''
  ElMessage.success('评论成功')
  load()
}

async function onFav() {
  const res = await toggleFavorite(Number(route.params.id))
  ElMessage.success(res.data.favorited ? '已收藏' : '已取消收藏')
}

async function onReport() {
  const { value } = await ElMessageBox.prompt('举报原因', '举报帖子').catch(() => ({ value: null }))
  if (!value) return
  await createReport({ targetType: 'POST', targetId: Number(route.params.id), reason: value })
  ElMessage.success('已提交举报')
}

async function onMark(id) {
  await markQuality(id)
  ElMessage.success('已标记优质')
  load()
}

onMounted(load)
</script>

<style scoped>
h2, h3 { color: #1b4332; }
.meta { display: flex; gap: 0.5rem; align-items: center; color: #8aa396; font-size: 0.85rem; margin-bottom: 0.75rem; }
.content { white-space: pre-wrap; line-height: 1.6; }
.ops { margin: 0.75rem 0 1rem; display: flex; gap: 0.5rem; }
.comment { background: #fff; border: 1px solid #e0ebe3; border-radius: 10px; padding: 0.7rem 0.85rem; margin-bottom: 0.5rem; }
.c-head { display: flex; gap: 0.4rem; align-items: center; margin-bottom: 0.3rem; }
.mt { margin-top: 0.5rem; }
</style>
