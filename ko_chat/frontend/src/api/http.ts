import type { ApiErrorResponse } from '../types/auth'
import { isAccessToken, isTokenExpired } from '../utils/crypto'

const baseUrl = import.meta.env.VITE_API_BASE_URL ?? ''

let unauthorizedHandler: (() => void) | null = null

export const setUnauthorizedHandler = (handler: () => void): void => {
  unauthorizedHandler = handler
}

const resolveToken = (token?: string | null): string | null => {
  const value = (token ?? localStorage.getItem('accessToken'))?.trim() || null
  if (!value) return null
  if (!isAccessToken(value) || isTokenExpired(value)) {
    localStorage.removeItem('accessToken')
    return null
  }
  return value
}

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
  const resolvedToken = resolveToken(token)
  if (resolvedToken) {
    headers.Authorization = `Bearer ${resolvedToken}`
  }
  return headers
}

export const checkBackendAvailability = async (): Promise<void> => {
  try {
    const response = await fetch(`${baseUrl}/api/v1/`)
    if (!response.ok) {
      throw new Error('서버에 연결할 수 없습니다')
    }
  } catch (error) {
    if (error instanceof ApiError) throw error
    throw new Error('서버에 연결할 수 없습니다. 백엔드(8080)가 실행 중인지 확인해 주세요.')
  }
}

const parseError = async (response: Response): Promise<ApiError> => {
  const errorBody = (await response.json().catch(() => ({}))) as ApiErrorResponse
  return new ApiError(
    response.status,
    errorBody.error ?? '요청 처리 중 오류가 발생했습니다.',
    errorBody.code,
  )
}

const handleUnauthorizedResponse = (response: Response): void => {
  if (response.status !== 401) return
  localStorage.removeItem('accessToken')
  unauthorizedHandler?.()
}

const requestJson = async <TResponse>(
  path: string,
  init: RequestInit,
): Promise<TResponse> => {
  let response: Response
  try {
    response = await fetch(`${baseUrl}${path}`, init)
  } catch {
    throw new ApiError(
      0,
      '서버에 연결할 수 없습니다. 백엔드(8080)가 실행 중인지 확인한 뒤 다시 시도해 주세요.',
    )
  }

  if (!response.ok) {
    handleUnauthorizedResponse(response)
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

export const postFormData = async <TResponse>(
  path: string,
  formData: FormData,
  token?: string | null,
): Promise<TResponse> =>
  requestJson(path, {
    method: 'POST',
    headers: buildHeaders(token),
    body: formData,
  })

export const downloadFile = async (
  path: string,
  token: string,
  filename: string,
): Promise<void> => {
  let response: Response
  try {
    response = await fetch(`${baseUrl}${path}`, {
      method: 'GET',
      headers: buildHeaders(token),
    })
  } catch {
    throw new ApiError(
      0,
      '서버에 연결할 수 없습니다. 백엔드(8080)가 실행 중인지 확인한 뒤 다시 시도해 주세요.',
    )
  }
  if (!response.ok) {
    handleUnauthorizedResponse(response)
    throw await parseError(response)
  }
  const blob = await response.blob()
  const url = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = filename
  anchor.click()
  URL.revokeObjectURL(url)
}
