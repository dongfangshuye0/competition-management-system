<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { http, type ApiRecord } from '../api/http'
import { useSessionStore } from '../stores/session'

const session = useSessionStore()
const rows = ref<ApiRecord[]>([])
const competitions = ref<ApiRecord[]>([])
const teachers = ref<ApiRecord[]>([])
const loading = ref(false)
const form = reactive({ competitionId: undefined as number | undefined, teacherId: undefined as number | undefined, teamName: '', teamMembers: '' })
const isStudent = computed(() => session.role === 'student')

async function load() {
  loading.value = true
  try {
    rows.value = await http.get(isStudent.value ? '/registrations/my' : session.role === 'teacher' ? '/registrations/teacher' : '/registrations')
    if (isStudent.value) {
      competitions.value = await http.get('/public/competitions')
      teachers.value = await http.get('/users/teachers')
    }
  } finally {
    loading.value = false
  }
}

async function submit() {
  const data = new FormData()
  Object.entries(form).forEach(([key, value]) => {
    if (value !== undefined && value !== '') data.append(key, String(value))
  })
  await http.post('/registrations', data)
  ElMessage.success('报名已提交')
  await load()
}

async function review(id: number, status: string) {
  await http.put(`/registrations/${id}/review`, { status, comment: status === 'approved' ? '材料完整，同意参赛' : '材料需补充' })
  ElMessage.success('审核已更新')
  await load()
}

onMounted(load)
</script>

<template>
  <section class="page" v-loading="loading">
    <h2>报名</h2>
    <el-card v-if="isStudent" shadow="never">
      <el-form :model="form" inline>
        <el-form-item label="竞赛">
          <el-select v-model="form.competitionId" filterable style="width: 240px">
            <el-option v-for="item in competitions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="指导教师">
          <el-select v-model="form.teacherId" filterable style="width: 180px">
            <el-option v-for="item in teachers" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="队伍"><el-input v-model="form.teamName" /></el-form-item>
        <el-form-item label="成员"><el-input v-model="form.teamMembers" /></el-form-item>
        <el-button type="primary" @click="submit">提交报名</el-button>
      </el-form>
    </el-card>

    <el-table :data="rows" border>
      <el-table-column prop="competitionName" label="竞赛" min-width="180" />
      <el-table-column prop="studentName" label="学生" width="120" />
      <el-table-column prop="teacherName" label="指导教师" width="120" />
      <el-table-column prop="teamName" label="队伍" width="140" />
      <el-table-column prop="statusLabel" label="状态" width="110" />
      <el-table-column prop="comment" label="意见" min-width="180" />
      <el-table-column v-if="!isStudent" label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="review(row.id, 'approved')">通过</el-button>
          <el-button size="small" type="danger" @click="review(row.id, 'rejected')">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>
