import { itemRepository } from '@/infrastructure/api/itemRepository'
import type { Item, ItemCreate, ItemUpdate } from '@/domain/item/Item'
import type { Page, Pageable } from '@/domain/item/types'

export const ItemUseCase = {
  async getPage(keyword: string | null, pageable: Pageable): Promise<Page<Item>> {
    return itemRepository.getPage(keyword, pageable)
  },

  async getById(id: string): Promise<Item | null> {
    try {
      return await itemRepository.getById(id)
    } catch {
      return null
    }
  },

  async create(dto: ItemCreate): Promise<Item> {
    return itemRepository.create({ name: dto.name, description: dto.description ?? '' })
  },

  async createBulk(dtos: ItemCreate[]): Promise<Item[]> {
    const list = dtos.map((d) => ({ name: d.name, description: d.description ?? '' }))
    return itemRepository.createBulk(list)
  },

  async update(id: string, dto: ItemUpdate): Promise<Item | null> {
    try {
      return await itemRepository.update(id, {
        name: dto.name ?? '',
        description: dto.description ?? '',
      })
    } catch {
      return null
    }
  },

  async delete(id: string): Promise<boolean> {
    try {
      await itemRepository.delete(id)
      return true
    } catch {
      return false
    }
  },
}
