<script setup lang="ts">
import { useRouter } from 'vue-router'

export interface SurveyNotification {
  surveyId: number
  title: string
  description: string | null
}

defineProps<{
  notification: SurveyNotification | null
}>()

const emit = defineEmits<{
  dismiss: []
  participate: [surveyId: number]
}>()

const router = useRouter()

const goToMySurveys = () => {
  emit('dismiss')
  router.push({ name: 'my-surveys' })
}
</script>

<template>
  <Teleport to="body">
    <div v-if="notification" class="survey-notification-overlay" @click.self="emit('dismiss')">
      <div class="survey-notification-card">
        <div class="survey-notification-icon">📋</div>
        <h3>새 설문조사가 도착했습니다</h3>
        <p class="survey-notification-title">{{ notification.title }}</p>
        <p v-if="notification.description" class="survey-notification-desc">
          {{ notification.description }}
        </p>
        <div class="survey-notification-actions">
          <button type="button" class="primary" @click="goToMySurveys">
            참여하기
          </button>
          <button type="button" class="secondary" @click="emit('dismiss')">
            나중에
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped src="../styles/components/SurveyNotificationPopup.css"></style>
