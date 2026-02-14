const BASE = '/api'

function buildUrl(path: string, params?: Record<string, string | number>): string {
  const url = new URL(BASE + path, window.location.origin)
  params && Object.entries(params).forEach(([k, v]) => url.searchParams.set(k, String(v)))
  return url.toString()
}

interface ErrorBody {
  message?: string
  error?: string
  status?: number
}

async function handleResponse<T>(res: Response): Promise<T> {
  if (!res.ok) {
    const text = await res.text().catch(() => res.statusText)
    let message = text
    try {
      const body: ErrorBody = JSON.parse(text)
      message = body.message ?? body.error ?? text
    } catch {
      /* use text as-is */
    }
    throw new Error(message || `HTTP ${res.status}`)
  }
  return res.json() as Promise<T>
}

export async function get<T>(path: string, params?: Record<string, string | number>): Promise<T> {
  return fetch(buildUrl(path, params), { method: 'GET', headers: { 'Content-Type': 'application/json' } }).then(
    (res) => handleResponse<T>(res)
  )
}

export async function post<T>(path: string, body: unknown): Promise<T> {
  return fetch(BASE + path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  }).then((res) => handleResponse<T>(res))
}

export async function put<T>(path: string, body: unknown): Promise<T> {
  return fetch(BASE + path, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  }).then((res) => handleResponse<T>(res))
}

export async function del(path: string): Promise<void> {
  const res = await fetch(BASE + path, { method: 'DELETE' })
  if (!res.ok && res.status !== 204) throw new Error(await res.text().catch(() => res.statusText))
}
