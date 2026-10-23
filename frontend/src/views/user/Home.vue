<template>
  <div class="home">
    <section class="hero">
      <h2>云农智诊</h2>
      <p>拍照识病 · 智能诊断 · 农技社区</p>
      <div class="actions">
        <el-button type="primary" size="large" @click="$router.push('/user/detect')">开始识别</el-button>
        <el-button size="large" @click="$router.push('/user/community')">进入社区</el-button>
      </div>
    </section>

    <el-carousel v-if="banners.length" height="140px" class="banner">
      <el-carousel-item v-for="b in banners" :key="b.id">
        <div class="banner-item">{{ b.title }}</div>
      </el-carousel-item>
    </el-carousel>

    <section v-if="notices.length" class="block">
      <h3>公告</h3>
      <div v-for="n in notices" :key="n.id" class="notice">{{ n.title }}：{{ n.content }}</div>
    </section>

    <section class="grid">
      <div class="tile" @click="$router.push('/user/history')">识别历史</div>
      <div class="tile" @click="$router.push('/user/disease')">病害库</div>
      <div class="tile" @click="$router.push('/user/news')">农业资讯</div>
      <div class="tile" @click="$router.push('/user/favorites')">我的收藏</div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { bannerList, noticeList } from '@/api/content'

const banners = ref([])
const notices = ref([])

onMounted(async () => {
  const [b, n] = await Promise.all([bannerList(), noticeList()])
  banners.value = b.data || []
  notices.value = (n.data || []).slice(0, 3)
})
</script>

<style scoped>
.hero {
  text-align: center;
  padding: 1.5rem 0.5rem 1rem;
}
.hero h2 {
  margin: 0;
  font-size: 1.8rem;
  color: #1b4332;
  letter-spacing: 0.12em;
}
.hero p { color: #52796f; margin: 0.5rem 0 1.2rem; }
.actions { display: flex; gap: 0.75rem; justify-content: center; flex-wrap: wrap; }
.banner { margin: 0.5rem 0 1rem; border-radius: 12px; overflow: hidden; }
.banner-item {
  height: 140px;
  display: grid;
  place-items: center;
  background: linear-gradient(120deg, #2d6a4f, #52b788);
  color: #fff;
  font-size: 1.1rem;
}
.block { margin-bottom: 1rem; }
.block h3 { margin: 0 0 0.5rem; color: #1b4332; font-size: 1rem; }
.notice {
  background: #fff;
  border: 1px solid #e0ebe3;
  border-radius: 10px;
  padding: 0.7rem 0.9rem;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
  color: #2d6a4f;
}
.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
}
.tile {
  background: #fff;
  border: 1px solid #dce8df;
  border-radius: 12px;
  padding: 1.2rem 1rem;
  text-align: center;
  color: #1b4332;
  font-weight: 600;
  cursor: pointer;
}
</style>
