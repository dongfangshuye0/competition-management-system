<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { http, type ApiRecord } from '../api/http'
import { useSessionStore } from '../stores/session'

type SummaryCard = {
  key: string
  label: string
  value: unknown
  to?: string
}

const session = useSessionStore()
const loading = ref(false)
const summary = ref<ApiRecord>({})
const summaryLabelMap: Record<string, string> = {
  unreadNotifications: '未读通知',
  users: '用户总数',
  competitions: '竞赛总数',
  registrations: '报名总数',
  works: '作品总数',
  pendingRegistrations: '待审核报名',
  pendingWorks: '待评审作品',
  assignedWorks: '分配作品',
  scoredWorks: '已评分作品',
  teachers: '教师总数',
  students: '学生总数',
  scores: '成绩总数',
  myRegistrations: '我的报名',
  approvedRegistrations: '已通过报名',
  myWorks: '我的作品',
  recommendedCompetitions: '推荐竞赛'
}
const summaryOrder = [
  'unreadNotifications',
  'users',
  'competitions',
  'registrations',
  'works',
  'pendingRegistrations',
  'pendingWorks',
  'assignedWorks',
  'scoredWorks',
  'teachers',
  'students',
  'scores',
  'myRegistrations',
  'approvedRegistrations',
  'myWorks',
  'recommendedCompetitions'
]

const summaryRouteMap: Record<string, Record<string, string>> = {
  admin: {
    unreadNotifications: '/admin/notifications',
    users: '/admin/users',
    competitions: '/admin/competitions',
    registrations: '/admin/registrations',
    works: '/admin/works',
    scores: '/admin/scores'
  },
  teacher: {
    unreadNotifications: '/teacher/notifications',
    pendingRegistrations: '/teacher/registrations',
    assignedWorks: '/teacher/reviews',
    scoredWorks: '/teacher/reviews'
  },
  student: {
    unreadNotifications: '/student/notifications',
    competitions: '/student/competitions',
    myRegistrations: '/student/registrations',
    approvedRegistrations: '/student/registrations',
    myWorks: '/student/works',
    recommendedCompetitions: '/student/competitions'
  }
}

const summaryCards = computed<SummaryCard[]>(() => {
  const entries = Object.entries(summary.value)
  const roleRoutes = summaryRouteMap[session.role] || {}
  return entries
    .sort(([leftKey], [rightKey]) => {
      const leftIndex = summaryOrder.indexOf(leftKey)
      const rightIndex = summaryOrder.indexOf(rightKey)
      if (leftIndex === -1 && rightIndex === -1) return leftKey.localeCompare(rightKey)
      if (leftIndex === -1) return 1
      if (rightIndex === -1) return -1
      return leftIndex - rightIndex
    })
    .map(([key, value]) => ({
      key,
      label: summaryLabelMap[key] || key.replace(/([A-Z])/g, ' $1').trim(),
      value: Array.isArray(value) ? value.length : value,
      to: roleRoutes[key]
    }))
})

async function load() {
  loading.value = true
  try {
    summary.value = await http.get('/dashboard')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="page" v-loading="loading">
    <h2>工作台</h2>
    <div class="stat-grid">
      <template v-for="item in summaryCards" :key="item.key">
        <a v-if="item.to" class="stat stat-action" :href="item.to" :aria-label="`查看${item.label}`">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <em>查看</em>
        </a>
        <div v-else class="stat">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </div>
      </template>
    </div>
  </section>
</template>
