<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { http, type ApiRecord } from '../api/http'
import { useSessionStore } from '../stores/session'

const session = useSessionStore()
const rows = ref<ApiRecord[]>([])
const registrations = ref<ApiRecord[]>([])
const teachers = ref<ApiRecord[]>([])
const loading = ref(false)
const submitForm = reactive({ registrationId: undefined as number | undefined, title: '', description: '' })
const scoreForm = reactive<Record<number, { score: number; comment: string }>>({})
const assignForm = reactive<Record<number, number | undefined>>({})
const isStudent = computed(() => session.role === 'student')
const isAdmin = computed(() => session.role === 'admin')

async function load() {
  loading.value = true
  try {
    rows.value = await http.get(isStudent.value ? '/works/my' : session.role === 'teacher' ? '/works/teacher' : '/works')
    rows.value.forEach((row) => {
      scoreForm[row.id] ||= { score: row.finalScore || 80, comment: row.finalComment || '' }
      assignForm[row.id] ||= row.reviewTeacherId
    })
    if (isStudent.value) registrations.value = await http.get('/registrations/my')
    if (isAdmin.value) teachers.value = await http.get('/users/teachers')
  } finally {
    loading.value = false
  }
}

async function submitWork() {
  const data = new FormData()
  Object.entries(submitForm).forEach(([key, value]) => {
    if (value !== undefined && value !== '') data.append(key, String(value))
  })
  await http.post('/works', data)
  ElMessage.success('作品已提交')
  await load()
}

async function assign(id: number) {
  await http.put(`/works/${id}/assign-reviewer`, { reviewerId: assignForm[id] })
  ElMessage.success('评审教师已分配')
  await load()
}

async function score(id: number) {
  await http.post(`/works/${id}/score`, scoreForm[id])
  ElMessage.success('评分已保存')
  await load()
}

onMounted(load)
</script>

<template>
  <section class="page" v-loading="loading">
    <h2>作品</h2>
    <el-card v-if="isStudent" shadow="never">
      <el-form :model="submitForm" inline>
        <el-form-item label="报名">
          <el-select v-model="submitForm.registrationId" style="width: 260px">
            <el-option v-for="item in registrations" :key="item.id" :label="item.competitionName" :value="item.id" :disabled="!item.canSubmitWork" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题"><el-input v-model="submitForm.title" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="submitForm.description" /></el-form-item>
        <el-button type="primary" @click="submitWork">提交作品</el-button>
      </el-form>
    </el-card>

    <el-table :data="rows" border>
      <el-table-column prop="competitionName" label="竞赛" min-width="170" />
      <el-table-column prop="studentName" label="学生" width="110" />
      <el-table-column prop="title" label="作品" min-width="160" />
      <el-table-column prop="aiScore" label="AI分" width="80" />
      <el-table-column prop="finalScore" label="最终分" width="90" />
      <el-table-column prop="finalComment" label="评语" min-width="200" />
      <el-table-column v-if="!isStudent" label="评分" width="280">
        <template #default="{ row }">
          <el-input-number v-model="scoreForm[row.id].score" :min="0" :max="100" size="small" />
          <el-input v-model="scoreForm[row.id].comment" size="small" placeholder="评语" style="width: 120px; margin-left: 8px" />
          <el-button size="small" type="primary" @click="score(row.id)">保存</el-button>
        </template>
      </el-table-column>
      <el-table-column v-if="isAdmin" label="分配" width="230">
        <template #default="{ row }">
          <el-select v-model="assignForm[row.id]" size="small" style="width: 130px">
            <el-option v-for="teacher in teachers" :key="teacher.id" :label="teacher.name" :value="teacher.id" />
          </el-select>
          <el-button size="small" @click="assign(row.id)">分配</el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>
