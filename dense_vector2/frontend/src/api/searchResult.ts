import type { SearchHit } from './searchHit'

export type SearchResult = {
  dims: number
  similarity: string
  k: number
  hits: SearchHit[]
}
