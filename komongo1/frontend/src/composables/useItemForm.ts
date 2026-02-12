import { ref, computed } from 'vue'
import { ItemUseCase } from '@/application/item/ItemUseCase'
import type { Item, ItemCreate, ItemUpdate } from '@/domain/item/Item'

export function useItemForm(onSuccess?: () => void) {
  const open = ref(false)
  const editingId = ref<string | null>(null)
  const name = ref('')
  const description = ref('')
  const loading = ref(false)
  const error = ref<string | null>(null)

  function openCreate() {
    editingId.value = null
    name.value = ''
    description.value = ''
    error.value = null
    open.value = true
  }

  function openEdit(item: Item) {
    editingId.value = item.id
    name.value = item.name
    description.value = item.description
    error.value = null
    open.value = true
  }

  function close() {
    open.value = false
    editingId.value = null
    name.value = ''
    description.value = ''
    error.value = null
  }

  async function submit() {
    loading.value = true
    error.value = null
    try {
      if (editingId.value) {
        await ItemUseCase.update(editingId.value, { name: name.value, description: description.value })
      } else {
        await ItemUseCase.create({ name: name.value, description: description.value })
      }
      close()
      onSuccess?.()
    } catch (e) {
      error.value = e instanceof Error ? e.message : '저장 실패'
    } finally {
      loading.value = false
    }
  }

  const isEdit = computed(() => !!editingId.value)

  return {
    open,
    editingId,
    name,
    description,
    loading,
    error,
    isEdit,
    openCreate,
    openEdit,
    close,
    submit,
  }
}
