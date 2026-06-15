<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete, Plus } from '@element-plus/icons-vue'
import { http, type ApiRecord } from '../api/http'

const loading = ref(false)
const rows = ref<ApiRecord[]>([])
const dialogOpen = ref(false)
const form = reactive<ApiRecord>({ username: '', password: '123456', name: '', role: 'student', major: '', className: '', phone: '', email: '' })

async function load() {
  loading.value = true
  try {
    rows.value = await http.get('/users')
  } finally {
    loading.value = false
  }
}

async function save() {
  await http.post('/users', form)
  ElMessage.success('用户已创建')
  dialogOpen.value = false
  await load()
}

async function remove(id: number) {
  await http.delete(`/users/${id}`)
  ElMessage.success('用户已删除')
  await load()
}

onMounted(load)
</script>

<template>
  <section class="page" v-loading="loading">
    <div class="toolbar">
      <h2>用户</h2>
      <el-button type="primary" :icon="Plus" @click="dialogOpen = true">新增用户</el-button>
    </div>
    <el-table :data="rows" border>
      <el-table-column prop="username" label="用户名" width="130" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="role" label="角色" width="100" />
      <el-table-column prop="major" label="专业" />
      <el-table-column prop="className" label="班级" />
      <el-table-column prop="phone" label="手机" width="140" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }"><el-button size="small" type="danger" :icon="Delete" @click="remove(row.id)" /></template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogOpen" title="新增用户" width="min(620px, 92vw)">
      <el-form :model="form" label-width="90px">
        <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="角色"><el-select v-model="form.role"><el-option label="学生" value="student" /><el-option label="教师" value="teacher" /><el-option label="管理员" value="admin" /></el-select></el-form-item>
        <el-form-item label="专业"><el-input v-model="form.major" /></el-form-item>
        <el-form-item label="班级"><el-input v-model="form.className" /></el-form-item>
        <el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
      </el-form>
      <template #footer><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </section>
</template>
