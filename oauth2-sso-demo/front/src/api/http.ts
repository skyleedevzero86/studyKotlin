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

export const postJson = async <TResponse>(
  path: string,
  body: unknown,
  token?: string | null,
): Promise<TResponse> => {
  const response = await fetch(`${baseUrl}${path}`, {
    method: 'POST',
    headers: buildHeaders(token, true),
    body: JSON.stringify(body),
  })

  if (!response.ok) {
    throw await parseError(response)
  }

  return (await response.json()) as TResponse
}

export const getJson = async <TResponse>(
  path: string,
  token?: string | null,
): Promise<TResponse> => {
  const response = await fetch(`${baseUrl}${path}`, {
    method: 'GET',
    headers: buildHeaders(token),
  })

  if (!response.ok) {
    throw await parseError(response)
  }

  return (await response.json()) as TResponse
}
