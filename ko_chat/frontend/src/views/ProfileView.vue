<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import { useProfile } from '../composables/useProfile'
import { formatRole, formatStatus } from '../utils/labels'

const router = useRouter()
const { accessToken, username, logout } = useAuth()
const {
  profile,
  profileForm,
  passwordForm,
  isLoading,
  errorMessage,
  successMessage,
  loadProfile,
  saveProfile,
  savePassword,
  withdrawAccount,
} = useProfile()

onMounted(async () => {
  if (!accessToken.value) {
    await router.push({ name: 'login' })
    return
  }
  await loadProfile(accessToken.value)
})

const handleSaveProfile = async () => {
  if (!accessToken.value) return
  await saveProfile(accessToken.value)
}

const handleChangePassword = async () => {
  if (!username.value) return
  await savePassword(username.value)
}

const handleWithdraw = async () => {
  if (!accessToken.value) return
  if (!confirm('정말 탈퇴하시겠습니까? 탈퇴 후 로그인할 수 없습니다.')) return
  const success = await withdrawAccount(accessToken.value)
  if (success) {
    logout()
    await router.push({ name: 'login' })
  }
}

const handleLogout = async () => {
  logout()
  await router.push({ name: 'login' })
}

const roleLabel = computed(() => formatRole(profile.value?.role))
const statusLabel = computed(() => formatStatus(profile.value?.status))
</script>

<template>
  <main class="profile-page">
    <section class="profile-card">
      <header class="profile-header">
        <div>
          <h1>내 정보</h1>
          <p v-if="profile">
            아이디: {{ profile.username }} · 권한: {{ roleLabel }} · 상태: {{ statusLabel }}
          </p>
        </div>
        <button type="button" class="secondary" @click="handleLogout">로그아웃</button>
      </header>

      <p v-if="errorMessage" class="error" role="alert">{{ errorMessage }}</p>
      <p v-if="successMessage" class="success" role="status">{{ successMessage }}</p>
      <p v-if="isLoading && !profile" class="hint">불러오는 중...</p>

      <template v-if="profile">
        <section class="profile-section">
          <h2>프로필 수정</h2>
          <form class="profile-form" @submit.prevent="handleSaveProfile">
            <label>
              표시 이름
              <input v-model="profileForm.displayName" type="text" maxlength="50" />
            </label>
            <button type="submit" :disabled="isLoading">저장</button>
          </form>
        </section>

        <section class="profile-section">
          <h2>계정 정보</h2>
          <dl class="info-list">
            <div><dt>가입일</dt><dd>{{ profile.createdAt }}</dd></div>
            <div><dt>비밀번호 변경일</dt><dd>{{ profile.passwordChangedAt }}</dd></div>
            <div><dt>마지막 로그인</dt><dd>{{ profile.lastLoginAt ?? '-' }}</dd></div>
            <div><dt>로그인 실패</dt><dd>{{ profile.loginFailCount }}회</dd></div>
            <div><dt>비밀번호 변경 실패</dt><dd>{{ profile.passwordChangeFailCount }}회</dd></div>
          </dl>
        </section>

        <section class="profile-section">
          <h2>비밀번호 변경</h2>
          <form class="profile-form" @submit.prevent="handleChangePassword">
            <label>
              현재 비밀번호
              <input v-model="passwordForm.currentPassword" type="password" required />
            </label>
            <label>
              새 비밀번호
              <input v-model="passwordForm.newPassword" type="password" required minlength="8" />
            </label>
            <label>
              새 비밀번호 확인
              <input v-model="passwordForm.confirmPassword" type="password" required minlength="8" />
            </label>
            <button type="submit" :disabled="isLoading">비밀번호 변경</button>
          </form>
        </section>

        <section class="profile-section danger-zone">
          <h2>회원 탈퇴</h2>
          <p class="hint">탈퇴 후에는 로그인할 수 없습니다.</p>
          <button type="button" class="danger" @click="handleWithdraw">탈퇴하기</button>
        </section>
      </template>
    </section>
  </main>
</template>
