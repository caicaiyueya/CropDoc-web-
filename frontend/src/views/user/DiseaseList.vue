<template>
  <div class="page">
    <h2>病害库</h2>
    <p class="tip">农户仅可见 BASIC；认证农技员可查看 PRO。</p>
    <el-select v-model="cropId" clearable placeholder="按作物筛选" style="width: 100%" class="mb" @change="load">
      <el-option v-for="c in crops" :key="c.id" :label="c.name" :value="c.id" />
    </el-select>
    <div v-for="d in list" :key="d.id" class="item" @click="open(d)">
      <div class="title">
        <el-tag size="small" :type="d.level === 'PRO' ? 'warning' : 'info'">{{ d.level }}</el-tag>
        {{ d.name }}
      </div>
      <div class="desc">{{ d.description }}</div>
    </div>
    <el-empty v-if="!list.length" description="暂无资料" />
    <el-dialog v-model="visible" :title="current?.name" width="90%">
      <p><strong>描述：</strong>{{ current?.description }}</p>
      <p><strong>防治建议：</strong>{{ current?.preventionAdvice }}</p>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { cropList, diseaseDetail, diseaseList } from '@/api/content'

const crops = ref([])
const cropId = ref(null)
const list = ref([])
const visible = ref(false)
const current = ref(null)

async function load() {
  const res = await diseaseList({ cropId: cropId.value || undefined, page: 1, size: 50 })
  list.value = res.data?.records || []
}

async function open(d) {
  try {
    const res = await diseaseDetail(d.id)
    current.value = res.data
    visible.value = true
  } catch (e) {
    ElMessage.error('无权查看或资料不存在')
  }
}

onMounted(async () => {
  const res = await cropList()
  crops.value = res.data || []
  load()
})
</script>

<style scoped>
h2 { color: #1b4332; margin-bottom: 0.25rem; }
.tip { color: #52796f; font-size: 0.88rem; margin-bottom: 0.75rem; }
.mb { margin-bottom: 0.75rem; }
.item { background: #fff; border: 1px solid #e0ebe3; border-radius: 10px; padding: 0.85rem 1rem; margin-bottom: 0.5rem; cursor: pointer; }
.title { display: flex; gap: 0.4rem; align-items: center; font-weight: 600; color: #1b4332; }
.desc { margin-top: 0.35rem; color: #6b7c72; font-size: 0.88rem; }
</style>
