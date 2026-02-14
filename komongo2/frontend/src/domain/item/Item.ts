export interface Item {
  id: string | null
  name: string
  description: string
  createdAt: string
  updatedAt: string
}

export type ItemCreate = Omit<Item, 'id' | 'createdAt' | 'updatedAt'> & {
  id?: null
  createdAt?: string
  updatedAt?: string
}

export type ItemUpdate = Partial<Pick<Item, 'name' | 'description'>>
