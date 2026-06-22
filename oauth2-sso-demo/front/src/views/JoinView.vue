<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useJoinForm } from '../composables/useJoinForm'

const router = useRouter()
const { form, errorMessage, successMessage, isLoading, submit } = useJoinForm()

const handleSubmit = async () => {
  const success = await submit()
  if (success) {
    await router.push({ name: 'login' })
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-card">
      <h1>회원가입</h1>
      <p class="subtitle">가입 후 관리자 승인이 필요합니다.</p>

      <form @submit.prevent="handleSubmit">
        <label>
          아이디
          <input v-model="form.username" type="text" autocomplete="username" required minlength="4" />
        </label>
        <label>
          표시 이름 (선택)
          <input v-model="form.displayName" type="text" maxlength="50" />
        </label>
        <label>
          비밀번호
          <input v-model="form.password" type="password" autocomplete="new-password" required minlength="8" />
        </label>
        <label>
          비밀번호 확인
          <input v-model="form.confirmPassword" type="password" autocomplete="new-password" required minlength="8" />
        </label>

        <p v-if="errorMessage" class="error" role="alert">{{ errorMessage }}</p>
        <p v-if="successMessage" class="success" role="status">{{ successMessage }}</p>

        <button type="submit" :disabled="isLoading">{{ isLoading ? '가입 중...' : '가입하기' }}</button>
      </form>

      <p class="hint">
        이미 계정이 있으신가요?
        <RouterLink to="/login">로그인</RouterLink>
      </p>
    </section>
  </main>
</template>
