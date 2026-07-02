import { computed, ref } from 'vue'
import type { PageResponse } from '../types/chat'

export const usePagination = (initialSize = 20) => {
  const page = ref(0)
  const size = ref(initialSize)
  const totalPages = ref(0)
  const totalElements = ref(0)

  const applyPageResponse = <T>(response: PageResponse<T>) => {
    page.value = response.number
    size.value = response.size
    totalPages.value = response.totalPages
    totalElements.value = response.totalElements
  }

  const resetPage = () => {
    page.value = 0
  }

  const hasPrev = computed(() => page.value > 0)
  const hasNext = computed(() => page.value + 1 < totalPages.value)
  const pageLabel = computed(() => {
    if (totalElements.value === 0) return '0건'
    return `${page.value + 1} / ${Math.max(totalPages.value, 1)} 페이지 (총 ${totalElements.value}건)`
  })

  const goPrev = () => {
    if (hasPrev.value) page.value -= 1
  }

  const goNext = () => {
    if (hasNext.value) page.value += 1
  }

  const goToPage = (target: number) => {
    if (target >= 0 && target < totalPages.value) {
      page.value = target
    }
  }

  return {
    page,
    size,
    totalPages,
    totalElements,
    hasPrev,
    hasNext,
    pageLabel,
    applyPageResponse,
    resetPage,
    goPrev,
    goNext,
    goToPage,
  }
}
