<template>
  <div>
    <div class="toolbar">
      <el-button @click="addCrop">新增作物</el-button>
      <el-button type="primary" @click="addDisease">新增病害</el-button>
    </div>
    <el-table :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="cropId" label="作物ID" width="90" />
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column prop="level" label="层级" width="90" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button link type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminDiseaseList, deleteDisease, saveCrop, saveDisease } from '@/api/admin'

const list = ref([])
async function load() {
  const res = await adminDiseaseList({ page: 1, size: 50 })
  list.value = res.data?.records || []
}
async function addCrop() {
  const { value } = await ElMessageBox.prompt('作物名称', '新增作物').catch(() => ({ value: null }))
  if (!value) return
  await saveCrop({ name: value })
  ElMessage.success('作物已添加')
}
async function addDisease() {
  const { value: cropId } = await ElMessageBox.prompt('作物ID', '新增病害').catch(() => ({ value: null }))
  if (!cropId) return
  const { value: name } = await ElMessageBox.prompt('病害名称', '新增病害').catch(() => ({ value: null }))
  if (!name) return
  const { value: level } = await ElMessageBox.prompt('层级 BASIC/PRO', '新增病害', { inputValue: 'BASIC' }).catch(() => ({ value: null }))
  await saveDisease({ cropId: Number(cropId), name, description: name, preventionAdvice: '详见田间管理', level: level || 'BASIC' })
  ElMessage.success('已保存')
  load()
}
async function remove(id) { await deleteDisease(id); load() }
onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; gap: 0.5rem; margin-bottom: 1rem; }
</style>
