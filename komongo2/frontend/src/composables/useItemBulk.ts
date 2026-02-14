import { ref, reactive } from 'vue'
import { ItemUseCase } from '@/application/item/ItemUseCase'
import type { ItemCreate } from '@/domain/item/Item'

const emptyRow = (): ItemCreate => ({ name: '', description: '' })

export function useItemBulk(onSuccess?: () => void) {
  const open = ref(false)
  const rows = ref<ItemCreate[]>([emptyRow()])
  const loading = ref(false)
  const error = ref<string | null>(null)

  function openModal() {
    rows.value = [emptyRow()]
    error.value = null
    open.value = true
  }

  function addRow() {
    rows.value = [...rows.value, emptyRow()]
  }

  function removeRow(index: number) {
    if (rows.value.length <= 1) return
    rows.value = rows.value.filter((_, i) => i !== index)
  }

  function updateRow(index: number, field: 'name' | 'description', value: string) {
    const next = [...rows.value]
    next[index] = { ...next[index], [field]: value }
    rows.value = next
  }

  async function submit() {
    const toCreate = rows.value.filter((r) => r.name.trim() !== '')
    if (toCreate.length === 0) {
      error.value = '이름을 입력한 행이 하나 이상 필요합니다.'
      return
    }
    loading.value = true
    error.value = null
    try {
      await ItemUseCase.createBulk(toCreate.map((r) => ({ name: r.name.trim(), description: r.description?.trim() ?? '' })))
      close()
      onSuccess?.()
    } catch (e) {
      error.value = e instanceof Error ? e.message : '일괄 등록 실패'
    } finally {
      loading.value = false
    }
  }

  function close() {
    open.value = false
    rows.value = [emptyRow()]
    error.value = null
  }

  return reactive({ open, rows, loading, error, openModal, addRow, removeRow, updateRow, submit, close })
}
