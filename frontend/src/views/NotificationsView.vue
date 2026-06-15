<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { http, type ApiRecord } from '../api/http'
import { useSessionStore } from '../stores/session'

const session = useSessionStore()
const rows = ref<ApiRecord[]>([])
const loading = ref(false)
const form = reactive({ title: '', content: '', broadcast: true })
const isAdmin = computed(() => session.role === 'admin')

async function load() {
  loading.value = true
  try {
    rows.value = await http.get('/notifications')
  } finally {
    loading.value = false
  }
}

async function markRead(id: number) {
  await http.put(`/notifications/${id}/read`)
  await load()
}

async function publish() {
  await http.post('/notifications', form)
  ElMessage.success('通知已发布')
  form.title = ''
  form.content = ''
  await load()
}

onMounted(load)
</script>

<template>
  <section class="page" v-loading="loading">
    <h2>通知</h2>
    <el-card v-if="isAdmin" shadow="never">
      <el-form :model="form" inline>
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" /></el-form-item>
        <el-button type="primary" @click="publish">发布</el-button>
      </el-form>
    </el-card>
    <el-table :data="rows" border>
      <el-table-column prop="title" label="标题" width="180" />
      <el-table-column prop="content" label="内容" />
      <el-table-column prop="read" label="已读" width="90" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }"><el-button size="small" :disabled="row.read" @click="markRead(row.id)">标记已读</el-button></template>
      </el-table-column>
    </el-table>
  </section>
</template>
