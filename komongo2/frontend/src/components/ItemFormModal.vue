<script setup lang="ts">
defineProps<{
  open: boolean
  isEdit: boolean
  name: string
  description: string
  loading: boolean
  error: string | null
}>()

const emit = defineEmits<{
  'update:name': [value: string]
  'update:description': [value: string]
  close: []
  submit: []
}>()
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="modal-overlay" @click.self="emit('close')">
      <div class="modal">
        <h2>{{ isEdit ? '항목 수정' : '항목 추가' }}</h2>
        <form @submit.prevent="emit('submit')">
          <div class="field">
            <label>이름</label>
            <input
              :value="name"
              @input="emit('update:name', ($event.target as HTMLInputElement).value)"
              type="text"
              required
            />
          </div>
          <div class="field">
            <label>설명</label>
            <textarea
              :value="description"
              @input="emit('update:description', ($event.target as HTMLTextAreaElement).value)"
              rows="3"
            />
          </div>
          <p v-if="error" class="error">{{ error }}</p>
          <div class="actions">
            <button type="button" class="cancel" @click="emit('close')">취소</button>
            <button type="submit" :disabled="loading">{{ loading ? '저장 중...' : '저장' }}</button>
          </div>
        </form>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { background: var(--bg); padding: 1.5rem; border-radius: 8px; min-width: 320px; box-shadow: 0 4px 20px rgba(0,0,0,0.2); }
.modal h2 { margin: 0 0 1rem; font-size: 1.25rem; }
.field { margin-bottom: 1rem; }
.field label { display: block; margin-bottom: 0.25rem; font-weight: 500; }
.field input, .field textarea { width: 100%; padding: 0.5rem; border: 1px solid var(--border); border-radius: 4px; font: inherit; }
.error { color: var(--danger); font-size: 0.875rem; margin-bottom: 0.5rem; }
.actions { display: flex; gap: 0.5rem; justify-content: flex-end; margin-top: 1rem; }
.actions button { padding: 0.5rem 1rem; border-radius: 4px; border: 1px solid var(--border); background: var(--bg); cursor: pointer; color: #e4e4e7; }
.actions button.cancel { background: var(--cancel-bg, #3f3f46); color: #e4e4e7; border-color: var(--border); }
.actions button.cancel:hover { background: #52525b; border-color: #52525b; }
.actions button[type='submit'] { background: var(--primary); color: white; border-color: var(--primary); }
.actions button:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
