export type Username = string & { readonly _brand: unique symbol }
export const Username = (s: string): Username => {
  if (s.trim().length === 0) throw new Error('사용자명을 입력해 주세요.')
  if (s.length > 64) throw new Error('사용자명은 1~64자여야 합니다.')
  return s as Username
}

export type TokenValue = string & { readonly _brand: unique symbol }
export const TokenValue = (s: string): TokenValue => {
  if (s.trim().length === 0) throw new Error('토큰 값을 입력해 주세요.')
  return s as TokenValue
}

export type AuthStep = 'password' | 'ott' | 'authenticated'

export interface AuthState {
  readonly step: AuthStep
  readonly username?: Username
}

export const AuthState = {
  initial: (): AuthState => ({ step: 'password' }),
  afterPassword: (username: Username): AuthState => ({ step: 'ott', username }),
  authenticated: (): AuthState => ({ step: 'authenticated' }),
} as const
