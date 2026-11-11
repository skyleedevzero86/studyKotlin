<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{ status: string; label?: string }>();

const tone = computed(() => {
  if (['COMPLETED', 'READY', 'DONE', 'APPLIED', 'STREAMING', 'APPROVED', 'STAGED'].includes(props.status)) return 'ok';
  if (['FAILED', 'REJECTED', 'ERROR'].includes(props.status)) return 'error';
  if (['COMPLETED_WITH_ISSUES', 'UNMAPPED', 'WAITING', 'PENDING', 'SNAPSHOTTING', 'LOADING', 'RUNNING', 'INFO', 'UNDER_REVIEW'].includes(props.status)) return 'warn';
  return 'muted';
});
</script>

<template>
  <span :class="['badge', `badge-${tone}`]">{{ label ?? status }}</span>
</template>
