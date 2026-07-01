<script setup lang="ts">
import { onMounted } from 'vue'
import { RouterView, useRouter } from 'vue-router'
import { setUnauthorizedHandler } from './api/http'
import { useAuth } from './composables/useAuth'

const router = useRouter()
const { logout, syncFromStorage } = useAuth()

onMounted(() => {
  syncFromStorage()
  setUnauthorizedHandler(() => {
    logout()
    void router.push({
      name: 'login',
      query: { reason: 'session-expired' },
    })
  })
})
</script>

<template>
  <RouterView />
</template>
