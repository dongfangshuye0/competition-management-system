<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { http } from '../api/http'
import { useSessionStore } from '../stores/session'

const router = useRouter()
const route = useRoute()
const session = useSessionStore()
const loading = ref(false)
const authMode = computed<'login' | 'register'>({
  get: () => (route.path === '/register' ? 'register' : 'login'),
  set: (mode) => {
    router.push(mode === 'register' ? '/register' : '/login')
  }
})
const loginForm = reactive({ username: 'admin', password: 'admin123' })
const registerForm = reactive({
  username: '',
  password: '',
  name: '',
  major: '',
  className: '',
  phone: '',
  email: ''
})
const demos = [
  ['管理员', 'admin', 'admin123'],
  ['教师', 'teacher01', 'teacher123'],
  ['学生', '20240001', 'student123']
]

async function login() {
  loading.value = true
  try {
    await session.login(loginForm.username, loginForm.password)
    ElMessage.success('登录成功')
    router.push(`/${session.role}/dashboard`)
  } finally {
    loading.value = false
  }
}

async function register() {
  loading.value = true
  try {
    await http.post('/auth/register', { ...registerForm })
    loginForm.username = registerForm.username
    loginForm.password = ''
    ElMessage.success('注册成功，请重新登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}

function fillDemo(username: string, password: string) {
  Object.assign(loginForm, { username, password })
  authMode.value = 'login'
}
</script>

<template>
  <div class="login-page">
    <RouterLink class="home-return" to="/" aria-label="返回首页">返回首页</RouterLink>
    <section class="login-shell" aria-label="账号入口">
      <div class="login-panel">
        <div class="login-title">
          <h2>{{ authMode === 'login' ? '账号登录' : '学生注册' }}</h2>
          <p>{{ authMode === 'login' ? '使用学号、工号或管理员账号登录' : '注册成功后请返回登录页重新登录' }}</p>
        </div>

        <el-form v-if="authMode === 'login'" :model="loginForm" class="auth-form" label-position="top" @submit.prevent="login">
          <div class="demo-grid">
            <button v-for="[label, username, password] in demos" :key="username" class="demo-card" type="button" @click="fillDemo(username, password)">
              <span>{{ label }}</span>
              <strong>{{ username }}</strong>
            </button>
          </div>
          <el-form-item label="账号">
            <el-input v-model="loginForm.username" autocomplete="username" size="large" placeholder="学号 / 工号 / 管理员账号" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="loginForm.password" type="password" show-password autocomplete="current-password" size="large" placeholder="请输入密码" />
          </el-form-item>
          <el-button class="auth-submit" type="primary" native-type="submit" :loading="loading">登录系统</el-button>
          <p class="auth-helper auth-helper-center">没有账号？<RouterLink to="/register">立即注册</RouterLink></p>
        </el-form>

        <el-form v-else :model="registerForm" class="auth-form register-grid" label-position="top" @submit.prevent="register">
          <el-form-item label="账号">
            <el-input v-model="registerForm.username" autocomplete="username" size="large" placeholder="学号" />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="registerForm.name" autocomplete="name" size="large" placeholder="真实姓名" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="registerForm.password" type="password" show-password autocomplete="new-password" size="large" placeholder="至少 6 位" />
          </el-form-item>
          <el-form-item label="专业">
            <el-input v-model="registerForm.major" size="large" placeholder="专业名称" />
          </el-form-item>
          <el-form-item label="班级">
            <el-input v-model="registerForm.className" size="large" placeholder="班级" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="registerForm.phone" autocomplete="tel" size="large" placeholder="手机号" />
          </el-form-item>
          <el-form-item class="field-full" label="邮箱">
            <el-input v-model="registerForm.email" autocomplete="email" size="large" placeholder="邮箱" />
          </el-form-item>
          <el-button class="auth-submit field-full" type="primary" native-type="submit" :loading="loading">注册账号</el-button>
          <p class="auth-helper field-full auth-helper-center">已有账号？<RouterLink to="/login">返回登录</RouterLink></p>
        </el-form>
      </div>
    </section>
  </div>
</template>
