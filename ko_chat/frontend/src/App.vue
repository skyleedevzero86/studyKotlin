<script setup lang="ts">
import { onMounted } from 'vue'
import { RouterView, useRouter } from 'vue-router'
import { setUnauthorizedHandler } from './api/http'
import SurveyNotificationPopup from './components/SurveyNotificationPopup.vue'
import { useAuth } from './composables/useAuth'
import {
  checkPendingSurveyNotifications,
  surveyNotification,
  useSurveyNotification,
} from './composables/useSurveyNotification'

const router = useRouter()
const { logout, syncFromStorage, accessToken } = useAuth()
const { dismissSurveyNotification } = useSurveyNotification()

onMounted(() => {
  syncFromStorage()
  const token = accessToken.value ?? localStorage.getItem('accessToken')?.trim()
  if (token) {
    void checkPendingSurveyNotifications(token)
  }
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
  <div class="app-shell">
    <RouterView />
    <SurveyNotificationPopup
      :notification="surveyNotification"
      @dismiss="dismissSurveyNotification"
    />
  </div>
</template>
