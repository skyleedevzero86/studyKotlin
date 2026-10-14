import type { PassageSummary } from './passageSummary'

export type PassageDetail = PassageSummary & {
  embedding: number[]
}
