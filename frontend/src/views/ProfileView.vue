<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { http } from '../api/http'
import { useSessionStore } from '../stores/session'

const session = useSessionStore()
const loading = ref(false)
const form = reactive({ name: '', major: '', className: '', phone: '', email: '', password: '' })

onMounted(() => {
  Object.assign(form, {
    name: session.user?.name || '',
    major: session.user?.major || '',
    className: session.user?.className || '',
    phone: session.user?.phone || '',
    email: session.user?.email || ''
  })
})

async function save() {
  loading.value = true
  try {
    await http.put('/users/me', form)
    await session.refreshMe()
    form.password = ''
    ElMessage.success('资料已保存')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="page" v-loading="loading">
    <h2>个人资料</h2>
    <el-card shadow="never">
      <el-form :model="form" label-width="90px">
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="专业"><el-input v-model="form.major" /></el-form-item>
        <el-form-item label="班级"><el-input v-model="form.className" /></el-form-item>
        <el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-button type="primary" @click="save">保存</el-button>
      </el-form>
    </el-card>
  </section>
</template>
