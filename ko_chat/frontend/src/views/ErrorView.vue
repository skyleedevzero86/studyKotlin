<script setup lang="ts">
import ErrorPage from '../components/ErrorPage.vue'

defineProps<{
  message?: string
}>()
</script>

<template>
  <ErrorPage :message="message" />
</template>
