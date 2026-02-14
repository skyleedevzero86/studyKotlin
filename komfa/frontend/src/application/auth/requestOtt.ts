import type { Username } from '../../domain/auth/types'

export interface RequestOttPort {
  requestOtt(username: Username): Promise<RequestOttResult>
}

export type RequestOttResult =
  | { ok: true }
  | { ok: false; error: string }

export async function requestOtt(
  port: RequestOttPort,
  username: Username
): Promise<RequestOttResult> {
  return port.requestOtt(username)
}
