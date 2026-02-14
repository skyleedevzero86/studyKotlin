import { ref, computed, watch, reactive } from 'vue'
import { ItemUseCase } from '@/application/item/ItemUseCase'
import { defaultPageable } from '@/domain/item/types'
import type { Item } from '@/domain/item/Item'
import type { Page, Pageable } from '@/domain/item/types'

export function useItemList() {
  const page = ref<Page<Item> | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)
  const pageable = ref<Pageable>(defaultPageable())
  const keyword = ref('')

  const items = computed(() => page.value?.content ?? [])
  const totalElements = computed(() => page.value?.totalElements ?? 0)
  const totalPages = computed(() => page.value?.totalPages ?? 0)
  const currentPage = computed(() => page.value?.number ?? 0)
  const size = computed(() => page.value?.size ?? 10)
  const isFirst = computed(() => page.value?.first ?? true)
  const isLast = computed(() => page.value?.last ?? true)

  async function fetchPage() {
    loading.value = true
    error.value = null
    try {
      page.value = await ItemUseCase.getPage(keyword.value.trim() || null, pageable.value)
    } catch (e) {
      error.value = e instanceof Error ? e.message : '목록 조회 실패'
    } finally {
      loading.value = false
    }
  }

  function setPage(p: number) {
    if (p >= 0 && p < totalPages.value) pageable.value = { ...pageable.value, page: p }
  }

  function setPageable(next: Partial<Pageable>) {
    pageable.value = { ...pageable.value, ...next }
  }

  function setKeyword(k: string) {
    keyword.value = k
    pageable.value = { ...pageable.value, page: 0 }
  }

  watch([pageable, keyword], fetchPage, { immediate: true })

  return reactive({
    items,
    totalElements,
    totalPages,
    currentPage,
    size,
    isFirst,
    isLast,
    loading,
    error,
    keyword,
    pageable,
    fetchPage,
    setPage,
    setPageable,
    setKeyword,
  })
}
