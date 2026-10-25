<template>
  <div class="admin-page">
    <div class="table-container">
      <div class="filter-bar">
        <el-input
          v-model="keyword"
          clearable
          placeholder="搜索病害名称"
          class="filter-input"
          @keyup.enter="load"
        />
        <el-select v-model="cropId" clearable placeholder="所属作物" class="filter-select">
          <el-option v-for="c in crops" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="onResetFilter">重置</el-button>
        <div class="filter-spacer" />
        <el-button @click="addCrop">新增作物</el-button>
        <el-button type="primary" @click="openEdit()">新增病害</el-button>
      </div>

      <el-table v-if="filteredList.length" :data="filteredList" v-loading="loading" stripe class="data-table">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="封面" width="88">
          <template #default="{ row }">
            <img class="thumb" :src="coverOf(row)" :alt="row.name" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" min-width="140" />
        <el-table-column label="所属作物" min-width="110">
          <template #default="{ row }">
            {{ cropName(row.cropId, row) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-popconfirm
              title="确定删除该病害资料？"
              confirm-button-text="删除"
              cancel-button-text="取消"
              @confirm="remove(row.id)"
            >
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else-if="!loading" description="暂无病害资料" :image-size="100" />
    </div>

    <el-dialog
      v-model="editVisible"
      :title="form.id ? '编辑病害' : '新增病害'"
      width="560px"
      destroy-on-close
      class="edit-dialog"
    >
      <el-form label-position="top">
        <el-form-item label="病害名称" required>
          <el-input v-model="form.name" placeholder="如：稻瘟病" />
        </el-form-item>
        <el-form-item label="所属作物" required>
          <el-select v-model="form.cropId" placeholder="选择作物" style="width: 100%">
            <el-option v-for="c in crops" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面图片">
          <div class="cover-edit">
            <img class="thumb lg" :src="form.coverUrl || placeholderCover" alt="封面预览" />
            <div class="cover-actions">
              <el-input v-model="form.coverUrl" placeholder="图片 URL 或上传后自动填入" />
              <el-upload
                :show-file-list="false"
                :http-request="onUploadCover"
                accept="image/jpeg,image/png,image/webp"
              >
                <el-button>上传图片</el-button>
              </el-upload>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="基础描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="症状与危害简述" />
        </el-form-item>
        <el-form-item label="发病原因">
          <el-input v-model="form.causes" type="textarea" :rows="2" placeholder="病原、传播途径等" />
        </el-form-item>
        <el-form-item label="防治措施">
          <el-input v-model="form.preventionAdvice" type="textarea" :rows="3" placeholder="农业/化学防治建议" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminDiseaseList, deleteDisease, saveCrop, saveDisease } from '@/api/admin'
import { cropList } from '@/api/content'
import { uploadFile } from '@/api/detection'
import { resolveStandardDiseaseImage } from '@/utils/diseaseCatalog'
import { ensureDemoSynced } from '@/utils/adminDemoSeed'

const COVER_KEY = 'ynzz_admin_disease_covers_v1'
const placeholderCover = '/images/hero-farm.jpg'

const list = ref([])
const crops = ref([])
const loading = ref(false)
const saving = ref(false)
const keyword = ref('')
const cropId = ref()
const editVisible = ref(false)
const coverMap = ref(loadCoverMap())

const form = reactive({
  id: null,
  name: '',
  cropId: null,
  description: '',
  causes: '',
  preventionAdvice: '',
  coverUrl: ''
})

const filteredList = computed(() => {
  let rows = list.value
  if (cropId.value) rows = rows.filter((r) => Number(r.cropId) === Number(cropId.value))
  const q = keyword.value.trim()
  if (q) rows = rows.filter((r) => String(r.name || '').includes(q))
  return rows
})

function loadCoverMap() {
  try {
    return JSON.parse(localStorage.getItem(COVER_KEY) || '{}')
  } catch {
    return {}
  }
}

function persistCover(id, url) {
  if (!id || !url) return
  coverMap.value = { ...coverMap.value, [String(id)]: url }
  localStorage.setItem(COVER_KEY, JSON.stringify(coverMap.value))
}

function cropName(id, row) {
  if (row?.cropName) return row.cropName
  return crops.value.find((c) => Number(c.id) === Number(id))?.name || `作物#${id || '—'}`
}

function coverOf(row) {
  const local = coverMap.value[String(row.id)]
  if (local) return local
  return resolveStandardDiseaseImage({ name: row.name }) || placeholderCover
}

function parseDescription(raw) {
  const text = String(raw || '')
  const causeMatch = text.match(/【发病原因】([\s\S]*?)(?:【发病条件】|【描述】|$)/)
  const condMatch = text.match(/【发病条件】([\s\S]*?)(?:【描述】|$)/)
  const descMatch = text.match(/【描述】\s*([\s\S]*)$/)
  const causes = [causeMatch?.[1], condMatch?.[1]].filter(Boolean).map((s) => s.trim()).join('\n')
  if (causeMatch || descMatch) {
    return { causes, description: (descMatch?.[1] || '').trim() }
  }
  return { causes: '', description: text }
}

function packDescription(description, causes) {
  if (!causes?.trim()) return description || ''
  return `【发病原因】${causes.trim()}\n【描述】${description || ''}`
}

async function loadCrops() {
  try {
    const res = await cropList()
    crops.value = res.data || []
  } catch {
    crops.value = []
  }
}

async function load() {
  loading.value = true
  try {
    let res = await adminDiseaseList({ page: 1, size: 100 })
    let records = res.data?.records || []
    if (!records.length) {
      await ensureDemoSynced()
      res = await adminDiseaseList({ page: 1, size: 100 })
      records = res.data?.records || []
    }
    list.value = records
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

function onResetFilter() {
  keyword.value = ''
  cropId.value = undefined
  load()
}

async function addCrop() {
  const { value } = await ElMessageBox.prompt('作物名称', '新增作物').catch(() => ({ value: null }))
  if (!value) return
  await saveCrop({ name: value })
  ElMessage.success('作物已添加')
  await loadCrops()
}

function openEdit(row) {
  if (row) {
    const parsed = parseDescription(row.description)
    form.id = row.id
    form.name = row.name || ''
    form.cropId = row.cropId
    form.description = parsed.description
    form.causes = parsed.causes
    form.preventionAdvice = row.preventionAdvice || ''
    form.coverUrl = coverMap.value[String(row.id)] || coverOf(row)
  } else {
    form.id = null
    form.name = ''
    form.cropId = crops.value[0]?.id || null
    form.description = ''
    form.causes = ''
    form.preventionAdvice = ''
    form.coverUrl = ''
  }
  editVisible.value = true
}

async function onUploadCover({ file }) {
  try {
    const res = await uploadFile(file)
    const path = res.data?.url || res.data?.path || res.data
    const url = typeof path === 'string' ? (path.startsWith('http') || path.startsWith('/') ? path : `/uploads/${path}`) : ''
    if (!url) throw new Error('上传失败')
    form.coverUrl = url
    ElMessage.success('封面已上传')
  } catch (e) {
    ElMessage.error(e?.message || '上传失败')
  }
}

async function save() {
  if (!form.name?.trim() || !form.cropId) {
    ElMessage.warning('请填写名称并选择作物')
    return
  }
  saving.value = true
  try {
    const payload = {
      id: form.id || undefined,
      cropId: Number(form.cropId),
      name: form.name.trim(),
      description: packDescription(form.description, form.causes),
      preventionAdvice: form.preventionAdvice || '详见田间管理'
    }
    const res = await saveDisease(payload)
    const savedId = res.data?.id || form.id
    if (savedId && form.coverUrl) persistCover(savedId, form.coverUrl)
    ElMessage.success('已保存')
    editVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function remove(id) {
  await deleteDisease(id)
  ElMessage.success('已删除')
  load()
}

onMounted(async () => {
  await loadCrops()
  await load()
})
</script>

<style scoped>
.admin-page {
  max-width: 1400px;
  margin: 0 auto;
}
.table-container {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  padding: 20px 20px 12px;
}
.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
  align-items: center;
}
.filter-input {
  width: 200px;
}
.filter-select {
  width: 160px;
}
.filter-spacer {
  flex: 1;
}
.thumb {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 8px;
  background: #eef2f0;
  display: block;
}
.thumb.lg {
  width: 72px;
  height: 72px;
}
.cover-edit {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  width: 100%;
}
.cover-actions {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>
