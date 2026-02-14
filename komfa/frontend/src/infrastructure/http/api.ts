export function apiOrigin(): string {
  if (typeof window === 'undefined') return 'http://localhost:8080'
  return window.location.port === '5173' ? 'http://localhost:8080' : ''
}
const base = (): string => apiOrigin()

const json = (body: unknown) => ({
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(body),
  credentials: 'include' as RequestCredentials,
})

export interface JoinError {
  code: string
  message: string
}

export async function join(username: string, password: string, email: string | null): Promise<{ ok: true } | { ok: false; status: number; error?: JoinError }> {
  const res = await fetch(`${base()}/api/join`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password, email: email || null }),
    credentials: 'include',
  })
  if (res.ok) return { ok: true }
  const data = res.status === 409 ? await res.json() : null
  return { ok: false, status: res.status, error: data }
}

export async function findUsername(email: string): Promise<{ ok: true; message: string } | { ok: false; code?: string; message: string }> {
  const res = await fetch(`${base()}/api/auth/find-username`, { ...json({ email }), method: 'POST' })
  const data = await res.json().catch(() => ({}))
  if (res.ok) return { ok: true, message: data.message ?? '' }
  return { ok: false, code: data.code, message: data.message ?? '탈퇴한 계정입니다. 관리자에게 문의하세요.' }
}

export async function forgotPassword(email: string): Promise<{ ok: true; message: string } | { ok: false; code?: string; message: string }> {
  const res = await fetch(`${base()}/api/auth/forgot-password`, { ...json({ email }), method: 'POST' })
  const data = await res.json().catch(() => ({}))
  if (res.ok) return { ok: true, message: data.message ?? '' }
  return { ok: false, code: data.code, message: data.message ?? '탈퇴한 계정입니다. 관리자에게 문의하세요.' }
}

export async function resetPassword(token: string, newPassword: string): Promise<{ ok: true } | { ok: false; message: string }> {
  const res = await fetch(`${base()}/api/auth/reset-password`, { ...json({ token, newPassword }), method: 'POST' })
  const data = await res.json().catch(() => ({}))
  if (res.ok) return { ok: true }
  return { ok: false, message: data.message ?? '링크가 만료되었거나 유효하지 않습니다.' }
}

export interface MeResponse {
  username: string
  email: string | null
  createdAt: string
  updatedAt: string
  requirePasswordChange: boolean
}

export async function me(): Promise<MeResponse | null> {
  try {
    const res = await apiFetch(`${base()}/api/me`)
    if (res.type === 'opaqueredirect' || res.status === 302 || res.status === 204 || !res.ok) return null
    return await res.json()
  } catch {
    return null
  }
}

export async function updateProfile(email: string | null): Promise<{ ok: true } | { ok: false }> {
  const res = await apiFetch(`${base()}/api/me`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email }),
  })
  return { ok: !(res.type === 'opaqueredirect' || res.status === 302) && res.ok }
}

export async function changePassword(currentPassword: string, newPassword: string): Promise<{ ok: true } | { ok: false }> {
  const res = await apiFetch(`${base()}/api/auth/change-password`, {
    ...json({ currentPassword, newPassword }),
    method: 'POST',
  })
  return { ok: !(res.type === 'opaqueredirect' || res.status === 302) && res.ok }
}

export async function withdraw(): Promise<{ ok: true; message: string } | { ok: false; message: string }> {
  const res = await apiFetch(`${base()}/api/me/withdraw`, { method: 'POST' })
  if (res.type === 'opaqueredirect' || res.status === 302) return { ok: false, message: '인증이 필요합니다.' }
  const data = await res.json().catch(() => ({}))
  if (res.ok) return { ok: true, message: data.message ?? '탈퇴되었습니다.' }
  return { ok: false, message: data.message ?? '실패했습니다.' }
}

const fetchNoRedirect = (url: string) =>
  fetch(url, { credentials: 'include', redirect: 'manual' })

const apiFetch = (url: string, init?: RequestInit) =>
  fetch(url, { ...init, credentials: 'include', redirect: 'manual' })

export async function userArea(): Promise<{ message: string; username: string } | null> {
  try {
    const res = await fetchNoRedirect(`${base()}/user`)
    if (res.type === 'opaqueredirect' || res.status === 302) {
      const loc = res.headers.get('location')
      if (typeof window !== 'undefined' && loc?.includes('/login')) {
        window.location.href = loc
      }
      return null
    }
    if (!res.ok) return null
    const ct = res.headers.get('content-type') ?? ''
    if (ct.includes('text/html')) return null
    return res.json()
  } catch {
    return null
  }
}

export async function adminArea(): Promise<{ message: string; username: string } | null> {
  try {
    const res = await fetchNoRedirect(`${base()}/admin`)
    if (res.type === 'opaqueredirect' || res.status === 302) {
      const loc = res.headers.get('location')
      if (typeof window !== 'undefined' && loc?.includes('/login')) {
        window.location.href = loc
      }
      return null
    }
    if (!res.ok) return null
    const ct = res.headers.get('content-type') ?? ''
    if (ct.includes('text/html')) return null
    return res.json()
  } catch {
    return null
  }
}

export interface AdminUserListItem {
  id: number
  username: string
  emailMasked: string | null
  roles: string
  status: string
  createdAt: string
  updatedAt: string
}

export interface AdminUserListResponse {
  content: AdminUserListItem[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export async function adminUsersList(page: number, size: number, search: string | null): Promise<AdminUserListResponse | null> {
  const q = search?.trim() || ''
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  if (q) params.set('search', q)
  const res = await apiFetch(`${base()}/api/admin/users?${params}`)
  if (res.type === 'opaqueredirect' || res.status === 302 || !res.ok) return null
  return res.json()
}

export interface PasswordHistoryItem {
  id: number
  changedAt: string
}

export interface AdminPasswordHistoryResponse {
  userId: number
  username: string
  content: PasswordHistoryItem[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export async function adminUsersPasswordHistory(userId: number, page: number, size: number): Promise<AdminPasswordHistoryResponse | null> {
  const res = await apiFetch(`${base()}/api/admin/users/${userId}/password-history?page=${page}&size=${size}`)
  if (res.type === 'opaqueredirect' || res.status === 302 || !res.ok) return null
  return res.json()
}

export async function adminUsersDecryptedEmail(userId: number): Promise<string | null> {
  const res = await apiFetch(`${base()}/api/admin/users/${userId}/sensitive/email`)
  if (res.type === 'opaqueredirect' || res.status === 302 || !res.ok) return null
  const data = await res.json()
  return data.email ?? null
}

export async function adminUsersApprove(userId: number): Promise<AdminUserListItem | { error: string }> {
  const res = await apiFetch(`${base()}/api/admin/users/${userId}/approve`, { method: 'POST' })
  if (res.type === 'opaqueredirect' || res.status === 302) return { error: '인증이 필요합니다.' }
  const data = await res.json().catch(() => ({}))
  if (res.ok) return data as AdminUserListItem
  return { error: data.message ?? '실패' }
}

export async function adminUsersSuspend(userId: number): Promise<AdminUserListItem | { error: string }> {
  const res = await apiFetch(`${base()}/api/admin/users/${userId}/suspend`, { method: 'POST' })
  if (res.type === 'opaqueredirect' || res.status === 302) return { error: '인증이 필요합니다.' }
  const data = await res.json().catch(() => ({}))
  if (res.ok) return data as AdminUserListItem
  return { error: data.message ?? '실패' }
}

export async function adminUsersWithdraw(userId: number): Promise<AdminUserListItem | { error: string }> {
  const res = await apiFetch(`${base()}/api/admin/users/${userId}/withdraw`, { method: 'POST' })
  if (res.type === 'opaqueredirect' || res.status === 302) return { error: '인증이 필요합니다.' }
  const data = await res.json().catch(() => ({}))
  if (res.ok) return data as AdminUserListItem
  return { error: data.message ?? '실패' }
}

export async function adminUsersUpdateRole(userId: number, roles: string): Promise<AdminUserListItem | { error: string }> {
  const res = await apiFetch(`${base()}/api/admin/users/${userId}/role`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ roles }),
  })
  if (res.type === 'opaqueredirect' || res.status === 302) return { error: '인증이 필요합니다.' }
  const data = await res.json().catch(() => ({}))
  if (res.ok) return data as AdminUserListItem
  return { error: data.message ?? '실패' }
}
