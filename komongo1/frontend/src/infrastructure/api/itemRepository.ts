import { get, post, put, del } from './client'
import type { Item } from '@/domain/item/Item'
import type { Page, Pageable, PageResponse } from '@/domain/item/types'
import { pagedModelToPage } from '@/domain/item/types'

const PATH = '/items'

export const itemRepository = {
  async getPage(keyword: string | null, pageable: Pageable): Promise<Page<Item>> {
    const params: Record<string, string | number> = {
      page: pageable.page,
      size: pageable.size,
      sortBy: pageable.sortBy,
      sortDir: pageable.sortDir,
    }
    if (keyword != null && keyword.trim() !== '') params.keyword = keyword.trim()
    const res = await get<PageResponse<Item>>(PATH, params)
    return pagedModelToPage(res)
  },

  getById(id: string): Promise<Item> {
    return get<Item>(`${PATH}/${id}`)
  },

  create(item: Omit<Item, 'id' | 'createdAt' | 'updatedAt'>): Promise<Item> {
    return post<Item>(PATH, { name: item.name, description: item.description ?? '' })
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
