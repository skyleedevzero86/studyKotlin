<script setup lang="ts">
import { useRouter } from 'vue-router'

const props = withDefaults(
  defineProps<{
    message?: string
    homeLabel?: string
  }>(),
  {
    message: '알 수 없는 에러가 발생했습니다. 다시 시도해 주세요.',
    homeLabel: '홈으로',
  },
)

const router = useRouter()

const goHome = async () => {
  const token = localStorage.getItem('accessToken')
  await router.push(token ? { name: 'home' } : { name: 'login' })
}
</script>

<template>
  <main class="error-page">
    <div class="error-page-content">
      <svg class="error-page-icon" viewBox="0 0 80 80" aria-hidden="true">
        <circle cx="40" cy="40" r="34" fill="none" stroke="currentColor" stroke-width="3" />
        <circle cx="30" cy="34" r="3" fill="currentColor" />
        <circle cx="50" cy="34" r="3" fill="currentColor" />
        <path
          d="M 28 50 Q 40 42 52 50"
          fill="none"
          stroke="currentColor"
          stroke-width="3"
          stroke-linecap="round"
        />
        <path
          d="M 22 26 Q 18 38 24 48"
          fill="none"
          stroke="currentColor"
          stroke-width="2.5"
          stroke-linecap="round"
        />
        <circle cx="21" cy="50" r="3" fill="currentColor" />
      </svg>

      <p class="error-page-message">{{ props.message }}</p>

      <button type="button" class="error-page-home" @click="goHome">
        {{ props.homeLabel }}
      </button>
    </div>
  </main>
</template>
