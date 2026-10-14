import { ApiRequestError } from './apiRequestError'
import type { PassageDetail } from './passageDetail'
import type { PassagePage } from './passagePage'
import type { SearchResult } from './searchResult'

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    ...init,
    headers: {
      Accept: 'application/json',
      ...(init?.body ? { 'Content-Type': 'application/json' } : {}),
      ...init?.headers,
    },
  })
  if (response.status === 204) {
    return undefined as T
  }
  const payload = (await response.json()) as { message?: string }
  if (!response.ok) {
    throw new ApiRequestError(payload.message ?? '요청을 처리하지 못했습니다.')
  }
  return payload as T
}

export function listPassages(page: number, size: number): Promise<PassagePage> {
  return request(`/api/passages?page=${page}&size=${size}`)
}

export function getPassage(id: number): Promise<PassageDetail> {
  return request(`/api/passages/${id}`)
}

export function createPassage(title: string, body: string): Promise<PassageDetail> {
  return request('/api/passages', {
    method: 'POST',
    body: JSON.stringify({ title, body }),
  })
}

export function updatePassage(id: number, title: string, body: string): Promise<PassageDetail> {
  return request(`/api/passages/${id}`, {
    method: 'PUT',
    body: JSON.stringify({ title, body }),
  })
}

export function deletePassage(id: number): Promise<void> {
  return request(`/api/passages/${id}`, { method: 'DELETE' })
}

export function searchPassages(query: string, k: number): Promise<SearchResult> {
  return request('/api/passages/search', {
    method: 'POST',
    body: JSON.stringify({ query, k }),
  })
}
