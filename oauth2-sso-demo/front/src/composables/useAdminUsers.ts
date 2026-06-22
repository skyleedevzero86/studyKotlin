import { ref } from 'vue'
import {
  activateUser,
  approveUser,
  changeUserRole,
  createAdminUser,
  deleteUser,
  fetchAdminUsers,
  resetLoginFailCount,
  resetPasswordFailCount,
  suspendUser,
  unlockUser,
  withdrawUserByAdmin,
} from '../api/userApi'
import { ApiError } from '../api/http'
import type { RevealedUserRow, UserSensitivePayload, UserSummaryResponse } from '../types/user'
import { decryptAes256Gcm, MASK_TEXT } from '../utils/crypto'

const ENCRYPTION_SECRET = import.meta.env.VITE_ENCRYPTION_SECRET ?? ''

const users = ref<RevealedUserRow[]>([])
const isLoading = ref(false)
const actionMessage = ref<string | null>(null)
const errorMessage = ref<string | null>(null)

const toRow = (user: UserSummaryResponse): RevealedUserRow => ({
  username: user.username,
  encryptedPayload: user.encryptedPayload,
  revealed: false,
})

const decryptPayload = async (encryptedPayload: string): Promise<UserSensitivePayload> => {
  const json = await decryptAes256Gcm(encryptedPayload, ENCRYPTION_SECRET)
  return JSON.parse(json) as UserSensitivePayload
}

export const useAdminUsers = () => {
  const loadUsers = async (token: string): Promise<void> => {
    isLoading.value = true
    errorMessage.value = null

    try {
      const response = await fetchAdminUsers(token)
      users.value = response.map(toRow)
    } catch (error) {
      errorMessage.value =
        error instanceof ApiError ? error.message : '사용자 목록을 불러오지 못했습니다.'
    } finally {
      isLoading.value = false
    }
  }

  const toggleReveal = async (username: string): Promise<void> => {
    const target = users.value.find((user) => user.username === username)
    if (!target) {
      return
    }

    if (target.revealed) {
      target.revealed = false
      target.sensitive = undefined
      target.decryptError = undefined
      return
    }

    try {
      target.sensitive = await decryptPayload(target.encryptedPayload)
      target.decryptError = undefined
      target.revealed = true
    } catch {
      target.decryptError = '복호화에 실패했습니다. 암호화 키를 확인하세요.'
      target.revealed = false
    }
  }

  const displayValue = (username: string, value: string | number | boolean | null): string => {
    const target = users.value.find((user) => user.username === username)
    if (!target?.revealed) {
      return MASK_TEXT
    }
    if (value === null || value === undefined || value === '') {
      return '-'
    }
    return String(value)
  }

  const runAction = async (token: string, action: () => Promise<{ message: string }>): Promise<void> => {
    actionMessage.value = null
    errorMessage.value = null
    try {
      const result = await action()
      actionMessage.value = result.message
      await loadUsers(token)
    } catch (error) {
      errorMessage.value = error instanceof ApiError ? error.message : '요청 처리에 실패했습니다.'
    }
  }

  const approve = (token: string, username: string, role: 'USER' | 'ADMIN') =>
    runAction(token, () => approveUser(token, username, role))

  const suspend = (token: string, username: string) =>
    runAction(token, () => suspendUser(token, username))

  const activate = (token: string, username: string) =>
    runAction(token, () => activateUser(token, username))

  const unlock = (token: string, username: string) =>
    runAction(token, () => unlockUser(token, username))

  const withdraw = (token: string, username: string) =>
    runAction(token, () => withdrawUserByAdmin(token, username))

  const changeRole = (token: string, username: string, role: 'USER' | 'ADMIN') =>
    runAction(token, () => changeUserRole(token, username, role))

  const remove = (token: string, username: string) =>
    runAction(token, () => deleteUser(token, username))

  const resetPwdFail = (token: string, username: string) =>
    runAction(token, () => resetPasswordFailCount(token, username))

  const resetLoginFail = (token: string, username: string) =>
    runAction(token, () => resetLoginFailCount(token, username))

  const addUser = (
    token: string,
    body: {
      username: string
      password: string
      displayName?: string | null
      role: 'USER' | 'ADMIN'
      activateImmediately: boolean
    },
  ) => runAction(token, () => createAdminUser(token, body))

  return {
    users,
    isLoading,
    errorMessage,
    actionMessage,
    loadUsers,
    toggleReveal,
    displayValue,
    approve,
    suspend,
    activate,
    unlock,
    withdraw,
    changeRole,
    remove,
    resetPwdFail,
    resetLoginFail,
    addUser,
  }
}
