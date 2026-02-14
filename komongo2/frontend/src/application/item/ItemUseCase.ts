import { itemRepository } from '@/infrastructure/api/itemRepository'
import type { Item, ItemCreate, ItemUpdate } from '@/domain/item/Item'
import type { Page, Pageable } from '@/domain/item/types'

function toCreateBody(dto: ItemCreate): { name: string; description: string } {
  return { name: dto.name, description: dto.description ?? '' }
}

function toUpdateBody(dto: ItemUpdate): { name: string; description: string } {
  return { name: dto.name ?? '', description: dto.description ?? '' }
}

export const ItemUseCase = {
  getPage(keyword: string | null, pageable: Pageable): Promise<Page<Item>> {
    return itemRepository.getPage(keyword, pageable)
  },

  async getById(id: string): Promise<Item | null> {
    try {
      return await itemRepository.getById(id)
    } catch {
      return null
    }
  },

  create(dto: ItemCreate): Promise<Item> {
    return itemRepository.create(toCreateBody(dto))
  },

  createBulk(dtos: ItemCreate[]): Promise<Item[]> {
    return itemRepository.createBulk(dtos.map(toCreateBody))
  },

  async update(id: string, dto: ItemUpdate): Promise<Item | null> {
    try {
      return await itemRepository.update(id, toUpdateBody(dto))
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
