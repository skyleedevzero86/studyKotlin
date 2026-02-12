const BASE = '/api'

async function handleNotOk(res: Response): Promise<never> {
  const status = res.status
  let message: string
  try {
    const text = await res.text()
    const parsed = text ? (JSON.parse(text) as Record<string, unknown>) : null
    if (parsed && (typeof parsed.message === 'string' || typeof parsed.error === 'string')) {
      message = (parsed.message ?? parsed.error) as string
    } else {
      message = text || res.statusText
    }
  } catch {
    message = res.statusText
  }
  if (status === 500) {
    message = `서버 오류 (500). ${message || '백엔드와 MongoDB가 실행 중인지 확인하세요.'}`
  } else if (status >= 500) {
    message = `서버 오류 (${status}). ${message}`
  }
  throw new Error(message)
}

export async function get<T>(path: string, params?: Record<string, string | number>): Promise<T> {
  const url = new URL(BASE + path, window.location.origin)
  if (params) {
    Object.entries(params).forEach(([k, v]) => url.searchParams.set(k, String(v)))
  }
  const res = await fetch(url.toString(), { method: 'GET', headers: { 'Content-Type': 'application/json' } })
  if (!res.ok) await handleNotOk(res)
  return res.json()
}

export async function post<T>(path: string, body: unknown): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  if (!res.ok) await handleNotOk(res)
  return res.json()
}

export async function put<T>(path: string, body: unknown): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  if (!res.ok) await handleNotOk(res)
  return res.json()
}

export async function del(path: string): Promise<void> {
  const res = await fetch(BASE + path, { method: 'DELETE' })
  if (!res.ok && res.status !== 204) await handleNotOk(res)
}
