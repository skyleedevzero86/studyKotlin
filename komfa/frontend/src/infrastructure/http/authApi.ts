import type { Username } from '../../domain/auth/types'
import type { RequestOttPort } from '../../application/auth/requestOtt'
import type { HealthPort } from '../../application/auth/health'
import type { HealthResult } from '../../application/auth/health'
import { apiOrigin } from './api'

const base = (): string => apiOrigin() || (typeof window !== 'undefined' ? '' : 'http://localhost:8080')

export const authApi: RequestOttPort = {
  async requestOtt(username: Username): Promise<import('../../application/auth/requestOtt').RequestOttResult> {
    const res = await fetch(`${base()}/ott/generate`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        Accept: 'application/json',
      },
      body: new URLSearchParams({ username: username as string }),
      credentials: 'include',
      redirect: 'manual',
    })
    if (res.status === 302 || res.type === 'opaqueredirect') {
      const loc = res.headers.get('location') ?? ''
      if (loc.includes('/ott/sent')) return { ok: true }
      if (typeof window !== 'undefined' && loc.includes('/login')) window.location.href = loc
      return { ok: false, error: '요청에 실패했습니다. 다시 시도해 주세요.' }
    }
    if (res.ok) return { ok: true }
    const text = await res.text()
    return { ok: false, error: text || '요청에 실패했습니다.' }
  },
}

export const healthApi: HealthPort = {
  async check(): Promise<HealthResult> {
    const res = await fetch(`${base()}/api/health`, { credentials: 'include' })
    if (!res.ok) throw new Error(`상태 확인 실패: ${res.status}`)
    return res.json()
  },
}
