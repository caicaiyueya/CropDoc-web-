<template>
  <div class="disease-page">
    <header class="page-head">
      <div>
        <h1>病害库</h1>
        <p>卡片浏览常见病虫害资料</p>
      </div>
      <el-select
        v-model="cropId"
        clearable
        placeholder="按作物筛选"
        class="crop-select"
        @change="load"
      >
        <el-option v-for="c in crops" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
    </header>

    <div v-if="loading" class="grid">
      <div v-for="i in 6" :key="i" class="disease-card skeleton-card">
        <el-skeleton animated :rows="3" />
      </div>
    </div>

    <div v-else-if="list.length" class="grid">
      <article
        v-for="d in list"
        :key="d.id"
        class="disease-card"
        @click="open(d)"
      >
        <div class="thumb-wrap">
          <img
            class="thumb"
            :src="d.image_url"
            :alt="d.name"
            :style="{ objectPosition: d.coverPos }"
          />
        </div>
        <div class="meta">
          <h3>{{ d.name }}</h3>
          <p>{{ d.summary || d.description }}</p>
        </div>
      </article>
    </div>

    <el-empty v-else description="暂无病害资料" :image-size="120" />

    <!-- 右侧抽屉详情 -->
    <el-drawer
      v-model="visible"
      size="480px"
      direction="rtl"
      :with-header="false"
      class="disease-drawer"
      destroy-on-close
    >
      <div v-if="current" class="drawer-body">
        <button type="button" class="drawer-close" @click="visible = false">×</button>

        <div class="hero-cover">
          <img :src="current.image_url" :alt="current.name" />
        </div>

        <div class="drawer-content">
          <div class="drawer-title-row">
            <h2>{{ current.name }}</h2>
          </div>

          <section class="block">
            <h4>基础描述</h4>
            <p>{{ current.description }}</p>
          </section>
          <section class="block">
            <h4>发病原因</h4>
            <p>{{ current.causes }}</p>
          </section>
          <section class="block">
            <h4>发病条件</h4>
            <p>{{ current.conditions }}</p>
          </section>
          <section class="block">
            <h4>防治措施</h4>
            <p>{{ current.prevention }}</p>
          </section>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { cropList, diseaseDetail, diseaseList } from '@/api/content'
import { mapDiseaseList, normalizeDisease } from '@/utils/diseaseCatalog'

const crops = ref([])
const cropId = ref(null)
const list = ref([])
const loading = ref(false)
const visible = ref(false)
const current = ref(null)

async function load() {
  loading.value = true
  try {
    const res = await diseaseList({
      cropId: cropId.value || undefined,
      page: 1,
      size: 24,
      pageSize: 24
    })
    list.value = mapDiseaseList(res.data?.records || [])
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

async function open(d) {
  try {
    const res = await diseaseDetail(d.id)
    current.value = normalizeDisease(res.data || d)
    visible.value = true
  } catch {
    current.value = normalizeDisease(d)
    visible.value = true
    ElMessage.warning('详情加载失败，已展示列表中的资料')
  }
}

onMounted(async () => {
  try {
    const res = await cropList()
    crops.value = res.data || []
  } catch {
    crops.value = []
  }
  await load()
})
</script>

<style scoped>
.disease-page {
  max-width: 1100px;
  margin: 0 auto;
  padding-bottom: 40px;
}

.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
  flex-wrap: wrap;
}
.page-head h1 {
  margin: 0 0 6px;
  font-size: 1.5rem;
  color: #163b1f;
  font-weight: 750;
}
.page-head p {
  margin: 0;
  color: #6b7280;
  font-size: 0.9rem;
}
.crop-select {
  width: 200px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  align-items: stretch;
}

.disease-card {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
  border: 1px solid #eef2f0;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.04);
  transition: border-color 0.15s ease, box-shadow 0.15s ease, transform 0.15s ease;
}
.disease-card:hover {
  border-color: #c5e8cd;
  box-shadow: 0 10px 24px rgba(40, 180, 70, 0.12);
  transform: translateY(-2px);
}
.skeleton-card {
  padding: 16px;
  cursor: default;
  min-height: 320px;
}
.skeleton-card:hover {
  transform: none;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.04);
}

/* 固定图片容器：宽满卡片、高 220px，裁切多余部分 */
.thumb-wrap {
  width: 100%;
  height: 220px;
  flex-shrink: 0;
  background-color: #f7f8fa;
  overflow: hidden;
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
  padding: 14px 16px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.meta h3 {
  margin: 0;
  font-size: 1.02rem;
  color: #1f2937;
  font-weight: 750;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.meta p {
  margin: 0;
  color: #6b7280;
  font-size: 0.84rem;
  line-height: 1.55;
  /* 固定两行描述，保证同行卡片文字区等高 */
  height: calc(1.55em * 2);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.drawer-body {
  position: relative;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
}
.drawer-close {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 2;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.92);
  color: #374151;
  font-size: 1.25rem;
  line-height: 1;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.12);
}
.hero-cover {
  flex-shrink: 0;
  width: 100%;
  height: 220px;
  background-color: #f7f8fa;
  overflow: hidden;
}
.hero-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  display: block;
}
.drawer-content {
  flex: 1;
  overflow: auto;
  padding: 20px 22px 28px;
}
.drawer-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
}
.drawer-title-row h2 {
  margin: 0;
  font-size: 1.35rem;
  color: #163b1f;
  font-weight: 800;
}
.block {
  margin-bottom: 18px;
  padding: 14px 16px;
  background: #f8faf9;
  border-radius: 12px;
  border: 1px solid #eef2f0;
}
.block h4 {
  margin: 0 0 8px;
  font-size: 0.92rem;
  color: #163b1f;
  font-weight: 750;
}
.block p {
  margin: 0;
  color: #4b5563;
  font-size: 0.9rem;
  line-height: 1.7;
}

@media (max-width: 960px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 600px) {
  .grid {
    grid-template-columns: 1fr;
  }
  .crop-select {
    width: 100%;
  }
}
</style>

<style>
/* 抽屉内容顶满，去掉默认内边距 */
.disease-drawer.el-drawer .el-drawer__body {
  padding: 0;
  overflow: hidden;
}
</style>
