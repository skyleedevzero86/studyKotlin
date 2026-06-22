<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useLoginForm } from '../composables/useLoginForm'
import { useAuth } from '../composables/useAuth'

const router = useRouter()
const { form, errorMessage, isLoading, submit } = useLoginForm()
const { isAdmin } = useAuth()

const handleSubmit = async () => {
  const success = await submit()
  if (success) {
    await router.push({ name: isAdmin.value ? 'admin-users' : 'home' })
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-card">
      <h1>로그인</h1>
      <p class="subtitle">OAuth2 SSO Demo · front</p>

      <form @submit.prevent="handleSubmit">
        <label>
          아이디
          <input
            v-model="form.username"
            type="text"
            autocomplete="username"
            placeholder="username"
            required
          />
        </label>

        <label>
          비밀번호
          <input
            v-model="form.password"
            type="password"
            autocomplete="current-password"
            placeholder="password"
            required
          />
        </label>

        <p v-if="errorMessage" class="error" role="alert">
          {{ errorMessage }}
        </p>

        <button type="submit" :disabled="isLoading">
          {{ isLoading ? '로그인 중...' : '로그인' }}
        </button>
      </form>

      <p class="hint">
        계정이 없으신가요?
        <RouterLink to="/join">회원가입</RouterLink>
      </p>
    </section>
  </main>
</template>
