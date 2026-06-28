<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'

const router = useRouter()
const { username, logout, isAdmin } = useAuth()

const handleLogout = async () => {
  logout()
  await router.push({ name: 'login' })
}

const goProfile = async () => {
  await router.push({ name: 'profile' })
}

const goChat = async () => {
  await router.push({ name: 'home' })
}

const goAdminUsers = async () => {
  await router.push({ name: 'admin-users' })
}
</script>

<template>
  <main class="home-page">
    <section class="home-card">
      <h1>환영합니다</h1>
      <p v-if="username">{{ username }}님, 로그인되었습니다.</p>

      <div class="actions">
        <button type="button" @click="goChat">채팅</button>
        <button type="button" @click="goProfile">내 정보</button>
        <button v-if="isAdmin" type="button" @click="goAdminUsers">관리자 사용자 목록</button>
        <button type="button" class="secondary" @click="handleLogout">로그아웃</button>
      </div>
    </section>
  </main>
</template>
