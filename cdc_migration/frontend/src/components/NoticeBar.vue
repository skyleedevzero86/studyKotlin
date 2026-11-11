<script setup lang="ts">
import type { Notice } from '../useNotice';

defineProps<{ notice: Notice | null }>();
</script>

<template>
  <div v-if="notice" :class="['notice', `notice-${notice.type}`]">{{ notice.text }}</div>
</template>
