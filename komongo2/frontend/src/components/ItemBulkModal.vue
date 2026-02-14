<script setup lang="ts">
import type { ItemCreate } from '@/domain/item/Item'

defineProps<{
  open: boolean
  rows: ItemCreate[]
  loading: boolean
  error: string | null
}>()

const emit = defineEmits<{
  addRow: []
  removeRow: [index: number]
  updateRow: [index: number, field: 'name' | 'description', value: string]
  submit: []
  close: []
}>()
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="modal-overlay" @click.self="emit('close')">
      <div class="modal bulk-modal">
        <h2>리스트로 일괄 입력</h2>
        <p class="hint">이름을 입력한 행만 등록됩니다.</p>
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>이름</th>
                <th>설명</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, i) in rows" :key="i">
                <td>
                  <input
                    :value="row.name"
                    @input="emit('updateRow', i, 'name', ($event.target as HTMLInputElement).value)"
                    type="text"
                    placeholder="이름"
                  />
                </td>
                <td>
                  <input
                    :value="row.description"
                    @input="emit('updateRow', i, 'description', ($event.target as HTMLInputElement).value)"
                    type="text"
                    placeholder="설명"
                  />
                </td>
                <td>
                  <button type="button" class="btn-remove" :disabled="rows.length <= 1" @click="emit('removeRow', i)">삭제</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <button type="button" class="btn-add" @click="emit('addRow')">+ 행 추가</button>
        <p v-if="error" class="error">{{ error }}</p>
        <div class="actions">
          <button type="button" class="cancel" @click="emit('close')">취소</button>
          <button type="button" class="primary" :disabled="loading" @click="emit('submit')">{{ loading ? '등록 중...' : '일괄 등록' }}</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { background: var(--bg); padding: 1.5rem; border-radius: 8px; box-shadow: 0 4px 20px rgba(0,0,0,0.2); max-height: 90vh; overflow: auto; }
.bulk-modal { min-width: 480px; }
.modal h2 { margin: 0 0 0.5rem; font-size: 1.25rem; }
.hint { font-size: 0.875rem; color: var(--muted); margin: 0 0 1rem; }
.table-wrap { overflow-x: auto; margin-bottom: 0.5rem; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 0.5rem; text-align: left; border-bottom: 1px solid var(--border); }
th { font-weight: 600; }
input { width: 100%; padding: 0.4rem; border: 1px solid var(--border); border-radius: 4px; font: inherit; }
.btn-add {
  padding: 0.5rem 1rem; margin-bottom: 1rem;
  border: 1px solid var(--success); border-radius: 6px;
  background: var(--success); color: white;
  cursor: pointer; font: inherit; font-weight: 500;
}
.btn-add:hover { background: #22c55e; border-color: #22c55e; }
.btn-remove {
  padding: 0.35rem 0.65rem; font-size: 0.875rem;
  border: 1px solid var(--danger); border-radius: 4px;
  background: var(--danger); color: white;
  cursor: pointer; font: inherit;
}
.btn-remove:hover:not(:disabled) { background: #dc2626; border-color: #dc2626; }
.btn-remove:disabled { opacity: 0.5; cursor: not-allowed; background: #7f1d1d; border-color: #7f1d1d; color: #fca5a5; }
.error { color: var(--danger); font-size: 0.875rem; margin-bottom: 0.5rem; }
.actions { display: flex; gap: 0.5rem; justify-content: flex-end; }
.actions button { padding: 0.5rem 1rem; border-radius: 4px; border: 1px solid var(--border); background: var(--bg); cursor: pointer; color: #e4e4e7; }
.actions button.cancel { background: var(--cancel-bg, #3f3f46); color: #e4e4e7; border-color: var(--border); }
.actions button.cancel:hover { background: #52525b; border-color: #52525b; }
.actions button.primary { background: var(--primary); color: white; border-color: var(--primary); }
.actions button:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
