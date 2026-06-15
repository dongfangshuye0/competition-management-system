import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useSessionStore } from '../stores/session'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import DashboardView from '../views/DashboardView.vue'
import CompetitionsView from '../views/CompetitionsView.vue'
import RegistrationsView from '../views/RegistrationsView.vue'
import WorksView from '../views/WorksView.vue'
import NotificationsView from '../views/NotificationsView.vue'
import UsersView from '../views/UsersView.vue'
import ProfileView from '../views/ProfileView.vue'
import QaView from '../views/QaView.vue'

const routes: RouteRecordRaw[] = [
  { path: '/', component: HomeView },
  { path: '/login', component: LoginView },
  { path: '/register', component: LoginView },
  { path: '/student/dashboard', component: DashboardView, meta: { roles: ['student'] } },
  { path: '/student/competitions', component: CompetitionsView, meta: { roles: ['student'] } },
  { path: '/student/registrations', component: RegistrationsView, meta: { roles: ['student'] } },
  { path: '/student/works', component: WorksView, meta: { roles: ['student'] } },
  { path: '/student/qa', component: QaView, meta: { roles: ['student'] } },
  { path: '/student/notifications', component: NotificationsView, meta: { roles: ['student'] } },
  { path: '/student/profile', component: ProfileView, meta: { roles: ['student'] } },
  { path: '/teacher/dashboard', component: DashboardView, meta: { roles: ['teacher'] } },
  { path: '/teacher/registrations', component: RegistrationsView, meta: { roles: ['teacher'] } },
  { path: '/teacher/reviews', component: WorksView, meta: { roles: ['teacher'] } },
  { path: '/teacher/notifications', component: NotificationsView, meta: { roles: ['teacher'] } },
  { path: '/teacher/profile', component: ProfileView, meta: { roles: ['teacher'] } },
  { path: '/admin/dashboard', component: DashboardView, meta: { roles: ['admin'] } },
  { path: '/admin/users', component: UsersView, meta: { roles: ['admin'] } },
  { path: '/admin/competitions', component: CompetitionsView, meta: { roles: ['admin'] } },
  { path: '/admin/registrations', component: RegistrationsView, meta: { roles: ['admin'] } },
  { path: '/admin/works', component: WorksView, meta: { roles: ['admin'] } },
  { path: '/admin/scores', component: CompetitionsView, meta: { roles: ['admin'] } },
  { path: '/admin/notifications', component: NotificationsView, meta: { roles: ['admin'] } },
  { path: '/admin/profile', component: ProfileView, meta: { roles: ['admin'] } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const session = useSessionStore()
  const roles = to.meta.roles as string[] | undefined
  if (to.path === '/') {
    return true
  }
  if (to.path === '/login' || to.path === '/register') {
    return session.isLoggedIn ? `/${session.role}/dashboard` : true
  }
  if (!session.isLoggedIn) {
    return '/login'
  }
  if (roles && !roles.includes(session.role)) {
    return `/${session.role}/dashboard`
  }
  return true
})

export default router
