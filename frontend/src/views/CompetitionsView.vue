<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { http, type ApiRecord } from '../api/http'
import { useSessionStore } from '../stores/session'

const session = useSessionStore()
const loading = ref(false)
const dialogOpen = ref(false)
const competitions = ref<ApiRecord[]>([])
const rankingRows = ref<ApiRecord[]>([])
const form = reactive<ApiRecord>({
  name: '',
  description: '',
  organizer: '',
  level: '校级',
  registrationStartTime: '',
  registrationEndTime: '',
  submissionEndTime: ''
})
const isAdmin = computed(() => session.role === 'admin')

async function load() {
  loading.value = true
  try {
    competitions.value = await http.get('/public/competitions')
  } finally {
    loading.value = false
  }
}

async function save() {
  await http.post('/competitions', form)
  ElMessage.success('竞赛已发布')
  dialogOpen.value = false
  await load()
}

async function publish(id: number) {
  await http.post(`/competitions/${id}/publish-ranking`)
  ElMessage.success('成绩已公布')
  await load()
}

async function showRanking(id: number) {
  rankingRows.value = await http.get(`/competitions/${id}/ranking`)
}

onMounted(load)
</script>

<template>
  <section class="page" v-loading="loading">
    <div class="toolbar">
      <h2>竞赛</h2>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
      <el-button v-if="isAdmin" type="primary" :icon="Plus" @click="dialogOpen = true">发布竞赛</el-button>
    </div>

    <el-table :data="competitions" border>
      <el-table-column prop="name" label="竞赛名称" min-width="180" />
      <el-table-column prop="level" label="级别" width="100" />
      <el-table-column prop="statusLabel" label="状态" width="120" />
      <el-table-column prop="registrationCount" label="报名数" width="90" />
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button size="small" @click="showRanking(row.id)">排名</el-button>
          <el-button v-if="isAdmin && !row.rankingPublished" size="small" type="primary" @click="publish(row.id)">公布成绩</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-table v-if="rankingRows.length" :data="rankingRows" border>
      <el-table-column prop="rank" label="名次" width="80" />
      <el-table-column prop="studentName" label="学生" />
      <el-table-column prop="teamName" label="队伍" />
      <el-table-column prop="title" label="作品" />
      <el-table-column prop="finalScore" label="得分" width="90" />
    </el-table>

    <el-dialog v-model="dialogOpen" title="发布竞赛" width="min(640px, 92vw)">
      <el-form :model="form" label-width="110px">
        <el-form-item label="竞赛名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
        <el-form-item label="主办方"><el-input v-model="form.organizer" /></el-form-item>
        <el-form-item label="级别"><el-select v-model="form.level"><el-option label="校级" value="校级" /><el-option label="省级" value="省级" /><el-option label="国家级" value="国家级" /></el-select></el-form-item>
        <el-form-item label="报名开始"><el-input v-model="form.registrationStartTime" placeholder="2026-07-01T00:00:00" /></el-form-item>
        <el-form-item label="报名截止"><el-input v-model="form.registrationEndTime" placeholder="2026-07-15T00:00:00" /></el-form-item>
        <el-form-item label="作品截止"><el-input v-model="form.submissionEndTime" placeholder="2026-08-01T00:00:00" /></el-form-item>
      </el-form>
      <template #footer><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </section>
</template>
