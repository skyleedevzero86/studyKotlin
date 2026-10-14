import type { PassageSummary } from './passageSummary'

export type PassagePage = {
  page: number
  size: number
  totalElements: number
  totalPages: number
  items: PassageSummary[]
}
