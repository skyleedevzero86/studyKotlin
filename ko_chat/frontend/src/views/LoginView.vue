<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useLoginForm } from '../composables/useLoginForm'

const router = useRouter()
const route = useRoute()
const { form, errorMessage, isLoading, submit } = useLoginForm()

const sessionNotice = computed(() =>
  route.query.reason === 'session-expired'
    ? '로그인이 만료되었습니다. 다시 로그인해 주세요.'
    : null,
)

const isAccountLocked = computed(() =>
  errorMessage.value?.includes('관리자에게 문의') ?? false,
)

const handleSubmit = async () => {
  const success = await submit()
  if (success) {
    await router.push({ name: 'home' })
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-card">
      <h1>로그인</h1>
      <p v-if="sessionNotice" class="hint" role="status">{{ sessionNotice }}</p>

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

        <div v-if="errorMessage" :class="['error', { 'error--locked': isAccountLocked }]" role="alert">
          <p>{{ errorMessage }}</p>
          <p v-if="isAccountLocked" class="error__contact">
            관리자 이메일: admin@kochat.com
          </p>
        </div>

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
