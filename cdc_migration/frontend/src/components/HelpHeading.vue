<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue';

defineProps<{ title: string }>();

const open = ref(false);
const root = ref<HTMLElement | null>(null);

function closeOutside(event: MouseEvent) {
  if (root.value && !root.value.contains(event.target as Node)) open.value = false;
}

function closeOnEscape(event: KeyboardEvent) {
  if (event.key === 'Escape') open.value = false;
}

onMounted(() => {
  document.addEventListener('click', closeOutside);
  document.addEventListener('keydown', closeOnEscape);
});

onUnmounted(() => {
  document.removeEventListener('click', closeOutside);
  document.removeEventListener('keydown', closeOnEscape);
});
</script>

<template>
  <h3 ref="root" class="help-heading">
    <button type="button" class="help-toggle" :aria-expanded="open" @click="open = !open">
      {{ title }}<span class="help-mark" aria-hidden="true">?</span>
    </button>
    <div v-if="open" class="bubble" role="tooltip"><slot /></div>
  </h3>
</template>
