export interface Pageable {
  page: number
  size: number
  sortBy: string
  sortDir: 'ASC' | 'DESC'
}

export interface PagedModel<T> {
  content: T[]
  metadata: {
    size: number
    number: number
    totalElements: number
    totalPages: number
  }
}

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
  numberOfElements: number
}

function emptyPage<T>(): Page<T> {
  return {
    content: [],
    totalElements: 0,
    totalPages: 0,
    size: 10,
    number: 0,
    first: true,
    last: true,
    numberOfElements: 0,
  }
}

export type PageResponse<T> =
  | PagedModel<T>
  | (Partial<Page<T>> & { content?: T[] })

function toNum(v: unknown): number {
  const num = Number(v)
  return Number.isNaN(num) ? 0 : num
}

export function pagedModelToPage<T>(p: PageResponse<T> | null | undefined): Page<T> {
  if (!p) return emptyPage<T>()
  const content = Array.isArray(p.content) ? p.content : []
  const hasMetadata = p && 'metadata' in p && p.metadata != null
  let totalElements: number
  let totalPages: number
  let size: number
  let number: number
  let first: boolean
  let last: boolean

  if (hasMetadata) {
    const m = (p as PagedModel<T>).metadata as Record<string, unknown>
    totalElements = toNum(m.totalElements ?? m.total_elements)
    totalPages = toNum(m.totalPages ?? m.total_pages)
    size = toNum(m.size) || 10
    number = toNum(m.number)
    first = number === 0
    last = (totalPages <= 1) || (number >= totalPages - 1)
  } else {
    const flat = p as Partial<Page<T>>
    totalElements = toNum(flat.totalElements)
    totalPages = Math.max(0, toNum(flat.totalPages)) || 1
    size = toNum(flat.size) || 10
    number = Math.max(0, toNum(flat.number))
    first = flat.first ?? true
    last = flat.last ?? true
  }

  if (content.length > 0 && totalElements === 0) {
    totalElements = content.length
    if (totalPages === 0) totalPages = 1
  }

  return {
    content,
    totalElements,
    totalPages,
    size,
    number,
    first,
    last,
    numberOfElements: content.length,
  }
}

export const defaultPageable = (): Pageable => ({
  page: 0,
  size: 10,
  sortBy: 'createdAt',
  sortDir: 'DESC',
})
