import { createRouter, createWebHistory } from 'vue-router'
import ChatView from '../views/ChatView.vue'
import ErrorView from '../views/ErrorView.vue'
import HomeView from '../views/HomeView.vue'
import JoinView from '../views/JoinView.vue'
import LoginView from '../views/LoginView.vue'
import ProfileView from '../views/ProfileView.vue'
import { isAdminRole, isTokenExpired, parseJwtRole } from '../utils/crypto'

const AdminChatRoomsView = () => import('../views/AdminChatRoomsView.vue')
const AdminStatisticsView = () => import('../views/AdminStatisticsView.vue')
const AdminMessagingView = () => import('../views/AdminMessagingView.vue')
const AdminUsersView = () => import('../views/AdminUsersView.vue')
const AdminSurveysView = () => import('../views/AdminSurveysView.vue')

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { guestOnly: true },
    },
    {
      path: '/join',
      name: 'join',
      component: JoinView,
      meta: { guestOnly: true },
    },
    {
      path: '/',
      name: 'home',
      component: ChatView,
      meta: { requiresAuth: true },
    },
    {
      path: '/welcome',
      name: 'welcome',
      component: HomeView,
      meta: { requiresAuth: true },
    },
    {
      path: '/profile',
      name: 'profile',
      component: ProfileView,
      meta: { requiresAuth: true },
    },
    {
      path: '/admin/users',
      name: 'admin-users',
      component: AdminUsersView,
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/chat-rooms',
      name: 'admin-chat-rooms',
      component: AdminChatRoomsView,
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/statistics',
      name: 'admin-statistics',
      component: AdminStatisticsView,
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/messaging',
      name: 'admin-messaging',
      component: AdminMessagingView,
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/surveys',
      name: 'admin-surveys',
      component: AdminSurveysView,
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/error',
      name: 'error',
      component: ErrorView,
      props: (route) => ({
        message:
          typeof route.query.message === 'string'
            ? route.query.message
            : '알 수 없는 에러가 발생했습니다. 다시 시도해 주세요.',
      }),
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: ErrorView,
      props: {
        message: '요청하신 페이지를 찾을 수 없습니다.',
      },
    },
  ],
})

router.beforeEach((to) => {
  const token = localStorage.getItem('accessToken')?.trim() ?? null
  const tokenExpired = token ? isTokenExpired(token) : true
  const isAuthenticated = Boolean(token) && !tokenExpired
  const role = isAuthenticated && token ? parseJwtRole(token) : null

  if (token && tokenExpired) {
    localStorage.removeItem('accessToken')
  }

  if (to.meta.requiresAuth && !isAuthenticated) {
    return { name: 'login', query: { reason: 'session-expired' } }
  }

  if (to.meta.guestOnly && isAuthenticated) {
    return { name: 'home' }
  }

  if (to.meta.requiresAdmin && !isAdminRole(role)) {
    return { name: 'home' }
  }

  return true
})

router.onError((error) => {
  console.error(error)
  void router.push({
    name: 'error',
    query: { message: '알 수 없는 에러가 발생했습니다. 다시 시도해 주세요.' },
  })
})

export default router
