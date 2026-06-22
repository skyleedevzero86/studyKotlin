import type { ApiErrorResponse } from '../types/auth'

const baseUrl = import.meta.env.VITE_API_BASE_URL ?? ''

export class ApiError extends Error {
  readonly status: number
  readonly code?: string

  constructor(status: number, message: string, code?: string) {
    super(message)
    this.status = status
    this.code = code
  }
}

const buildHeaders = (token?: string | null, contentType = false): HeadersInit => {
  const headers: Record<string, string> = {}
  if (contentType) {
    headers['Content-Type'] = 'application/json'
  }
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }
  return headers
}

const parseError = async (response: Response): Promise<ApiError> => {
  const errorBody = (await response.json().catch(() => ({}))) as ApiErrorResponse
  return new ApiError(
    response.status,
    errorBody.error ?? '요청 처리 중 오류가 발생했습니다.',
    errorBody.code,
  )
}

const requestJson = async <TResponse>(
  path: string,
  init: RequestInit,
): Promise<TResponse> => {
  const response = await fetch(`${baseUrl}${path}`, init)

  if (!response.ok) {
    throw await parseError(response)
  }

  if (response.status === 204) {
    return undefined as TResponse
  }

  return (await response.json()) as TResponse
}

export const postJson = async <TResponse>(
  path: string,
  body: unknown,
  token?: string | null,
): Promise<TResponse> =>
  requestJson(path, {
    method: 'POST',
    headers: buildHeaders(token, true),
    body: JSON.stringify(body),
  })

export const getJson = async <TResponse>(
  path: string,
  token?: string | null,
): Promise<TResponse> =>
  requestJson(path, {
    method: 'GET',
    headers: buildHeaders(token),
  })

export const putJson = async <TResponse>(
  path: string,
  body: unknown,
  token?: string | null,
): Promise<TResponse> =>
  requestJson(path, {
    method: 'PUT',
    headers: buildHeaders(token, true),
    body: JSON.stringify(body),
  })

export const deleteJson = async <TResponse>(
  path: string,
  token?: string | null,
): Promise<TResponse> =>
  requestJson(path, {
    method: 'DELETE',
    headers: buildHeaders(token),
  })
