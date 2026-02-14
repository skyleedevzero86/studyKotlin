export interface Pageable {
  page: number
  size: number
  sortBy: string
  sortDir: 'ASC' | 'DESC'
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

export const defaultPageable = (): Pageable => ({
  page: 0,
  size: 10,
  sortBy: 'createdAt',
  sortDir: 'DESC',
})
