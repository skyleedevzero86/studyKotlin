import { get, post, put, del } from './client'
import type { Item } from '@/domain/item/Item'
import type { Page, Pageable } from '@/domain/item/types'

const PATH = '/items'

interface PagedModelResponse<T> {
  content: T[]
  metadata?: { size: number; number: number; totalElements: number; totalPages: number }
}

function toPage<T>(res: PagedModelResponse<T> | Page<T>): Page<T> {
  if ('metadata' in res && res.metadata) {
    const m = res.metadata
    return {
      content: res.content,
      totalElements: m.totalElements,
      totalPages: m.totalPages,
      size: m.size,
      number: m.number,
      first: m.number === 0,
      last: m.number >= m.totalPages - 1,
      numberOfElements: res.content.length,
    }
  }
  return res as Page<T>
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
