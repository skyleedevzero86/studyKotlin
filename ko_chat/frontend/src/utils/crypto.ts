const encoder = new TextEncoder()

const fromBase64 = (value: string): Uint8Array => {
  const binary = atob(value)
  const bytes = new Uint8Array(binary.length)
  for (let index = 0; index < binary.length; index += 1) {
    bytes[index] = binary.charCodeAt(index)
  }
  return bytes
}

const sha256 = (value: string): Promise<ArrayBuffer> =>
  crypto.subtle.digest('SHA-256', encoder.encode(value))

const importAesKey = async (secret: string): Promise<CryptoKey> => {
  const keyBytes = await sha256(secret)
  return crypto.subtle.importKey('raw', keyBytes, { name: 'AES-GCM' }, false, ['decrypt'])
}

export const decryptAes256Gcm = async (
  encryptedBase64: string,
  secret: string,
): Promise<string> => {
  const combined = fromBase64(encryptedBase64)
  const iv = combined.slice(0, 12)
  const ciphertext = combined.slice(12)
  const key = await importAesKey(secret)

  const decrypted = await crypto.subtle.decrypt({ name: 'AES-GCM', iv }, key, ciphertext)
  return new TextDecoder().decode(decrypted)
}

export const parseJwtRole = (token: string): string | null => {
  const payload = parseJwtPayload(token)
  return payload?.role ?? null
}

export const parseJwtSubject = (token: string): string | null => {
  const payload = parseJwtPayload(token)
  return payload?.sub ?? null
}

const parseJwtPayload = (
  token: string,
): { sub?: string; role?: string; exp?: number; tokenType?: string } | null => {
  const parts = token.split('.')
  if (parts.length !== 3) {
    return null
  }

  const payloadSegment = parts[1] ?? ''
  const normalized = payloadSegment.replace(/-/g, '+').replace(/_/g, '/')
  const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=')
  try {
    return JSON.parse(atob(padded)) as {
      sub?: string
      role?: string
      exp?: number
      tokenType?: string
    }
  } catch {
    return null
  }
}

export const isTokenExpired = (token: string, skewSeconds = 30): boolean => {
  const payload = parseJwtPayload(token)
  if (!payload?.exp) return false
  return Date.now() >= payload.exp * 1000 - skewSeconds * 1000
}

export const isAccessToken = (token: string): boolean => {
  const payload = parseJwtPayload(token)
  return payload?.tokenType === 'ACCESS'
}

export const isAdminRole = (role: string | null): boolean =>
  role === 'ROLE_ADMIN' || role === 'ADMIN'

export const MASK_TEXT = '••••••••'
