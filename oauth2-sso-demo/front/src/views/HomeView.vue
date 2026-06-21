<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'

const router = useRouter()
const { accessToken, logout, isAdmin } = useAuth()

const handleLogout = async () => {
  logout()
  await router.push({ name: 'login' })
}

const goAdminUsers = async () => {
  await router.push({ name: 'admin-users' })
}
</script>

<template>
  <main class="home-page">
    <section class="home-card">
      <h1>환영합니다</h1>
      <p>JWT 로그인에 성공했습니다.</p>

      <div class="token-box">
        <span class="label">Access Token</span>
        <code>{{ accessToken }}</code>
      </div>

      <div class="actions">
        <button v-if="isAdmin" type="button" @click="goAdminUsers">관리자 사용자 목록</button>
        <button type="button" class="secondary" @click="handleLogout">로그아웃</button>
      </div>
    </section>
  </main>
</template>
