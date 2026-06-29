import { createRouter, createWebHistory } from 'vue-router'
import AdminUsersView from '../views/AdminUsersView.vue'
import ChatView from '../views/ChatView.vue'
import ErrorView from '../views/ErrorView.vue'
import HomeView from '../views/HomeView.vue'
import JoinView from '../views/JoinView.vue'
import LoginView from '../views/LoginView.vue'
import ProfileView from '../views/ProfileView.vue'
import { isAdminRole, parseJwtRole } from '../utils/crypto'

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
  const token = localStorage.getItem('accessToken')
  const isAuthenticated = Boolean(token)
  const role = token ? parseJwtRole(token) : null

  if (to.meta.requiresAuth && !isAuthenticated) {
    return { name: 'login' }
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
