<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { SwitchButton } from '@element-plus/icons-vue'
import { useSessionStore } from './stores/session'

const session = useSessionStore()
const router = useRouter()
const route = useRoute()
const isPublicRoute = computed(() => route.path === '/' || route.path === '/login' || route.path === '/register')
const roleLabelMap: Record<string, string> = {
  admin: '管理员',
  teacher: '教师',
  student: '学生'
}
const currentRoleLabel = computed(() => roleLabelMap[session.role] || session.role)

const navItems = computed(() => {
  const role = session.role
  if (role === 'student') {
    return [
      ['工作台', '/student/dashboard'],
      ['竞赛列表', '/student/competitions'],
      ['我的报名', '/student/registrations'],
      ['我的作品', '/student/works'],
      ['智能答疑', '/student/qa'],
      ['通知中心', '/student/notifications'],
      ['个人资料', '/student/profile']
    ]
  }
  if (role === 'teacher') {
    return [
      ['工作台', '/teacher/dashboard'],
      ['报名审核', '/teacher/registrations'],
      ['作品评审', '/teacher/reviews'],
      ['通知中心', '/teacher/notifications'],
      ['个人资料', '/teacher/profile']
    ]
  }
  if (role === 'admin') {
    return [
      ['工作台', '/admin/dashboard'],
      ['用户管理', '/admin/users'],
      ['竞赛管理', '/admin/competitions'],
      ['报名总览', '/admin/registrations'],
      ['作品分配', '/admin/works'],
      ['成绩排名', '/admin/scores'],
      ['通知管理', '/admin/notifications'],
      ['个人资料', '/admin/profile']
    ]
  }
  return []
})

function logout() {
  session.logout()
  router.push('/login')
}
</script>

<template>
  <RouterView v-if="isPublicRoute" />
  <div v-else class="shell">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark">竞</span>
        <span>高校竞赛管理系统</span>
      </div>
      <nav class="nav" aria-label="主导航">
        <RouterLink v-for="[label, path] in navItems" :key="path" :to="path">{{ label }}</RouterLink>
      </nav>
    </aside>
    <main class="main">
      <header class="topbar">
        <div>
          <strong>{{ session.user?.name }}</strong>
          <span class="role-pill">{{ currentRoleLabel }}</span>
        </div>
        <el-button :icon="SwitchButton" @click="logout">退出</el-button>
      </header>
      <RouterView />
    </main>
  </div>
</template>
