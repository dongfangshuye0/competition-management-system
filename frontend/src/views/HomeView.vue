<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Calendar, Menu, Search, Trophy, User } from '@element-plus/icons-vue'
import heroImage from '../assets/home-hero-campus.png'
import { http, type ApiRecord } from '../api/http'
import { useSessionStore } from '../stores/session'

const router = useRouter()
const session = useSessionStore()
const competitions = ref<ApiRecord[]>([])
const loading = ref(false)
const activeNav = ref<'home' | 'competitions'>('home')
type HomeCompetition = ApiRecord & {
  id?: string | number
  name: string
  level?: string
  organizer?: string
  statusLabel?: string
  registrationCount?: number
  theme?: string
}

const fallbackCompetitions: HomeCompetition[] = [
  { id: 'demo-code', name: '编程竞赛', level: '校级', organizer: '信息工程学院', statusLabel: '报名中', registrationCount: 128, theme: 'green' },
  { id: 'demo-english', name: '英语竞赛', level: '省级', organizer: '外国语学院', statusLabel: '报名中', registrationCount: 96, theme: 'mint' },
  { id: 'demo-physics', name: '物理竞赛', level: '国家级', organizer: '理学院', statusLabel: '评审中', registrationCount: 84, theme: 'blue' },
  { id: 'demo-subject', name: '学科竞赛', level: '校级', organizer: '教务处', statusLabel: '即将开始', registrationCount: 72, theme: 'purple' }
]

const displayCompetitions = computed(() => {
  const source = competitions.value.length
    ? [...(competitions.value as HomeCompetition[]), ...fallbackCompetitions].slice(0, 4)
    : fallbackCompetitions
  return source.slice(0, 4).map((item, index) => ({
    ...item,
    theme: item.theme || ['green', 'mint', 'blue', 'purple'][index % 4]
  }))
})
const registrationPath = computed(() => {
  if (!session.isLoggedIn) {
    return '/login'
  }
  return session.role === 'student' ? '/student/competitions' : `/${session.role}/dashboard`
})

async function load() {
  loading.value = true
  try {
    competitions.value = await http.get('/public/competitions')
  } catch {
    // 接口不可用时保留前台首页的视觉完整性，真实数据恢复后会自动替换。
    competitions.value = []
  } finally {
    loading.value = false
  }
}

function goPrimary() {
  router.push(session.isLoggedIn ? `/${session.role}/dashboard` : '/login')
}

function goHome() {
  activeNav.value = 'home'
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function scrollToCompetitions() {
  activeNav.value = 'competitions'
  document.querySelector('#hot-competitions')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function goRegistration() {
  if (!session.isLoggedIn) {
    router.push('/login')
    return
  }
  router.push(session.role === 'student' ? '/student/competitions' : `/${session.role}/dashboard`)
}

onMounted(load)
</script>

<template>
  <main id="home-top" class="home-page">
    <header class="portal-header">
      <RouterLink class="portal-brand" to="/" aria-label="学科竞赛系统首页">
        <span class="portal-brand-icon"><el-icon><Menu /></el-icon></span>
        <strong>学科竞赛系统</strong>
      </RouterLink>
      <nav class="portal-nav" aria-label="前台导航">
        <a href="#home-top" :class="{ active: activeNav === 'home' }" @click="goHome">首页</a>
        <a href="#hot-competitions" :class="{ active: activeNav === 'competitions' }" @click="activeNav = 'competitions'">赛事大厅</a>
        <RouterLink :to="registrationPath">赛事报名</RouterLink>
      </nav>
      <button class="portal-user" type="button" @click="goPrimary">
        <span class="portal-avatar"><el-icon><User /></el-icon></span>
        <span>{{ session.isLoggedIn ? session.user?.name : '登录' }}</span>
      </button>
    </header>

    <section class="portal-hero" aria-label="大学生数字竞赛宣传">
      <img :src="heroImage" alt="大学生数字竞赛" />
    </section>

    <section id="hot-competitions" class="portal-section" v-loading="loading">
      <div class="portal-section-title">
        <h1>热门竞赛</h1>
        <p>赛出风格 赛出精彩</p>
      </div>

      <div class="competition-showcase">
        <article v-for="item in displayCompetitions" :key="item.id || item.name" class="competition-card" :class="`theme-${item.theme}`">
          <div class="competition-art">
            <span class="art-disc"></span>
            <span class="art-ribbon"></span>
            <span class="art-dot"></span>
          </div>
          <div class="competition-info">
            <strong>{{ item.name }}</strong>
            <span>{{ item.level }} · {{ item.organizer }}</span>
          </div>
          <div class="competition-meta">
            <span><el-icon><Trophy /></el-icon>{{ item.statusLabel || '进行中' }}</span>
            <span><el-icon><Calendar /></el-icon>{{ Number(item.registrationCount || 0) }} 人报名</span>
          </div>
        </article>
      </div>
    </section>

    <section class="portal-quick">
      <button type="button" @click="scrollToCompetitions">
        <el-icon><Search /></el-icon>
        <span>查找赛事</span>
      </button>
      <button type="button" @click="goRegistration">
        <el-icon><Trophy /></el-icon>
        <span>报名参赛</span>
      </button>
    </section>

    <footer id="site-footer" class="portal-footer" aria-label="网站底部信息">
      <div class="portal-footer-inner">
        <div class="footer-brand-mark" aria-hidden="true">竞</div>

        <div class="footer-info">
          <nav class="footer-links" aria-label="底部导航">
            <RouterLink to="/">高校竞赛服务平台</RouterLink>
            <span aria-hidden="true">|</span>
            <RouterLink to="/login">赛事管理入口</RouterLink>
            <span aria-hidden="true">|</span>
            <a href="#hot-competitions" @click="activeNav = 'competitions'">热门竞赛</a>
            <span aria-hidden="true">|</span>
            <a href="#site-footer">联系我们</a>
          </nav>
          <p>版权所有：高校竞赛管理系统</p>
          <p>备案信息：待配置　公安备案：待配置</p>
        </div>

        <div class="footer-qrs" aria-label="官方二维码">
          <div class="footer-qr-item">
            <div class="footer-qr-code qr-blue" aria-hidden="true">
              <span class="qr-corner qr-top-left"></span>
              <span class="qr-corner qr-top-right"></span>
              <span class="qr-corner qr-bottom-left"></span>
              <span class="qr-noise"></span>
              <span class="qr-logo">竞</span>
            </div>
            <span>竞赛官方微信平台</span>
          </div>
          <div class="footer-qr-item">
            <div class="footer-qr-code qr-red" aria-hidden="true">
              <span class="qr-corner qr-top-left"></span>
              <span class="qr-corner qr-top-right"></span>
              <span class="qr-corner qr-bottom-left"></span>
              <span class="qr-noise"></span>
              <span class="qr-logo">赛</span>
            </div>
            <span>赛事通知微信号</span>
          </div>
        </div>
      </div>
    </footer>
  </main>
</template>
