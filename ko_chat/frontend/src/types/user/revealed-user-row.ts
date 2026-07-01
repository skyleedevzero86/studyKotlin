import type { UserSensitivePayload } from './user-sensitive-payload'

export type RevealedUserRow = {
  username: string
  encryptedPayload: string
  revealed: boolean
  sensitive?: UserSensitivePayload
  decryptError?: string
}
