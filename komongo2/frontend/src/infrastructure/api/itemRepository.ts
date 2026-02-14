import { get, post, put, del } from './client'
import type { Item } from '@/domain/item/Item'
import type { Page, Pageable } from '@/domain/item/types'

const PATH = '/items'

interface PagedModelResponse<T> {
  content: T[]
  metadata?: {
    size?: number
    number?: number
    totalElements?: number
    totalPages?: number
    total_elements?: number
    total_pages?: number
  }
}

function toPage<T>(res: PagedModelResponse<T> | Page<T>): Page<T> {
  if ('metadata' in res && res.metadata) {
    const m = res.metadata
    const totalElementsRaw = m.totalElements ?? m.total_elements ?? 0
    const totalPagesRaw = m.totalPages ?? m.total_pages ?? 1
    const totalElements = Math.max(totalElementsRaw, res.content?.length ?? 0)
    const totalPages = Math.max(totalPagesRaw, totalElements > 0 ? 1 : 0)
    return {
      content: res.content ?? [],
      totalElements,
      totalPages: totalPages > 0 ? totalPages : 1,
      size: m.size ?? res.content?.length ?? 10,
      number: m.number ?? 0,
      first: (m.number ?? 0) === 0,
      last: (m.number ?? 0) >= totalPages - 1,
      numberOfElements: res.content?.length ?? 0,
    }
  }
  const flat = res as Page<T>
  const total = flat.totalElements ?? 0
  const contentLen = flat.content?.length ?? 0
  return {
    ...flat,
    content: flat.content ?? [],
    totalElements: total >= contentLen ? total : contentLen,
    totalPages: Math.max(flat.totalPages ?? 1, total >= contentLen ? (flat.totalPages ?? 1) : 1),
  }
}

function toParams(pageable: Pageable, keyword: string | null): Record<string, string | number> {
  const params: Record<string, string | number> = {
    page: pageable.page,
    size: pageable.size,
    sortBy: pageable.sortBy,
    sortDir: pageable.sortDir,
  }
  if (keyword != null && keyword.trim() !== '') params.keyword = keyword.trim()
  return params
}

export const itemRepository = {
  async getPage(keyword: string | null, pageable: Pageable): Promise<Page<Item>> {
    const res = await get<PagedModelResponse<Item> | Page<Item>>(PATH, toParams(pageable, keyword))
    return toPage(res)
  },
  getById(id: string): Promise<Item> {
    return get<Item>(`${PATH}/${id}`)
  },
  create(item: Omit<Item, 'id' | 'createdAt' | 'updatedAt'>): Promise<Item> {
    return post<Item>(PATH, item)
  },
  createBulk(items: Array<Omit<Item, 'id' | 'createdAt' | 'updatedAt'>>): Promise<Item[]> {
    return post<Item[]>(`${PATH}/bulk`, items)
  },
  update(id: string, item: Pick<Item, 'name' | 'description'>): Promise<Item> {
    return put<Item>(`${PATH}/${id}`, item)
  },
  delete(id: string): Promise<void> {
    return del(`${PATH}/${id}`)
  },
}
