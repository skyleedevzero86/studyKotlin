import type { Username } from '../../domain/auth/types'
import type { RequestOttPort } from '../../application/auth/requestOtt'
import type { HealthPort } from '../../application/auth/health'
import type { HealthResult } from '../../application/auth/health'

const base = (): string => (typeof window !== 'undefined' ? '' : 'http://localhost:8080')

export const authApi: RequestOttPort = {
  async requestOtt(username: Username): Promise<import('../../application/auth/requestOtt').RequestOttResult> {
    const res = await fetch(`${base()}/ott/generate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({ username: username as string }),
      credentials: 'include',
    })
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
