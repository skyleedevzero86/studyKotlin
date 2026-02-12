import { ref } from 'vue'
import { ItemUseCase } from '@/application/item/ItemUseCase'
import type { Item } from '@/domain/item/Item'

export function useItemDelete(onSuccess?: () => void) {
  const open = ref(false)
  const target = ref<Item | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  function confirm(item: Item) {
    target.value = item
    error.value = null
    open.value = true
  }

  async function doDelete() {
    const id = target.value?.id
    if (!id) return
    loading.value = true
    error.value = null
    try {
      const ok = await ItemUseCase.delete(id)
      if (ok) {
        close()
        onSuccess?.()
      } else {
        error.value = '삭제할 수 없습니다.'
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : '삭제 실패'
    } finally {
      loading.value = false
    }
  }

  function close() {
    open.value = false
    target.value = null
    error.value = null
  }

  return {
    open,
    target,
    loading,
    error,
    confirm,
    doDelete,
    close,
  }
}
