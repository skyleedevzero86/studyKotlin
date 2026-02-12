<script setup lang="ts">
import type { Item } from '@/domain/item/Item'

defineProps<{
  open: boolean
  target: Item | null
  loading: boolean
  error: string | null
}>()

const emit = defineEmits<{
  confirm: []
  close: []
}>()
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="modal-overlay" @click.self="emit('close')">
      <div class="modal">
        <h2>삭제 확인</h2>
        <p v-if="target">
          <strong>{{ target.name }}</strong> 항목을 삭제할까요?
        </p>
        <p v-if="error" class="error">{{ error }}</p>
        <div class="actions">
          <button type="button" class="cancel" @click="emit('close')">취소</button>
          <button type="button" class="danger" :disabled="loading" @click="emit('confirm')">
            {{ loading ? '삭제 중...' : '삭제' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal {
  background: var(--bg);
  padding: 1.5rem;
  border-radius: 8px;
  min-width: 320px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}
.modal h2 {
  margin: 0 0 1rem;
  font-size: 1.25rem;
}
.error {
  color: var(--danger);
  font-size: 0.875rem;
  margin: 0.5rem 0;
}
.actions {
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
  margin-top: 1rem;
}
.actions button {
  padding: 0.5rem 1rem;
  border-radius: 4px;
  border: 1px solid var(--border);
  background: var(--bg);
  cursor: pointer;
}
.actions button.cancel {
  background: var(--danger);
  color: white;
  border-color: var(--danger);
}
.actions button.danger {
  background: var(--danger);
  color: white;
  border-color: var(--danger);
}
.actions button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
