import { createRouter, createWebHistory } from 'vue-router'
import AdminUsersView from '../views/AdminUsersView.vue'
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

export default router
