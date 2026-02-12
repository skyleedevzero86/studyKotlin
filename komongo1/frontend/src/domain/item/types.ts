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

export function pagedModelToPage<T>(p: PagedModel<T> | null | undefined): Page<T> {
  if (!p?.metadata) return emptyPage<T>()
  const m = p.metadata
  const content = Array.isArray(p.content) ? p.content : []
  return {
    content,
    totalElements: Number(m.totalElements) || 0,
    totalPages: Number(m.totalPages) || 0,
    size: Number(m.size) || 10,
    number: Number(m.number) || 0,
    first: m.number === 0,
    last: (m.totalPages <= 1) || (m.number >= m.totalPages - 1),
    numberOfElements: content.length,
  }
}

export const defaultPageable = (): Pageable => ({
  page: 0,
  size: 10,
  sortBy: 'createdAt',
  sortDir: 'DESC',
})
