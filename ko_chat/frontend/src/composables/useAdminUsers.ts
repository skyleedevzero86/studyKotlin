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
  restoreUser,
  suspendUser,
  unlockUser,
  updateUserProfileByAdmin,
  withdrawUserByAdmin,
} from '../api/userApi'
import { ApiError } from '../api/http'
import type { PageResponse } from '../types/chat'
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
  const loadUsers = async (token: string, page = 0, size = 20): Promise<PageResponse<UserSummaryResponse> | void> => {
    isLoading.value = true
    errorMessage.value = null

    try {
      const response = await fetchAdminUsers(token, page, size)
      users.value = response.content.map(toRow)
      return response
    } catch (error) {
      errorMessage.value =
        error instanceof ApiError ? error.message : '사용자 목록을 불러오지 못했습니다.'
      throw error
    } finally {
      isLoading.value = false
    }
  }

  const runAction = async (
    token: string,
    action: () => Promise<{ message: string }>,
    page = 0,
    size = 20,
  ): Promise<void> => {
    actionMessage.value = null
    errorMessage.value = null
    try {
      const result = await action()
      actionMessage.value = result.message
      await loadUsers(token, page, size)
    } catch (error) {
      errorMessage.value = error instanceof ApiError ? error.message : '요청 처리에 실패했습니다.'
    }
  }

  const approve = (token: string, username: string, role: 'USER' | 'ADMIN', page = 0, size = 20) =>
    runAction(token, () => approveUser(token, username, role), page, size)

  const suspend = (token: string, username: string, page = 0, size = 20) =>
    runAction(token, () => suspendUser(token, username), page, size)

  const activate = (token: string, username: string, page = 0, size = 20) =>
    runAction(token, () => activateUser(token, username), page, size)

  const restore = (token: string, username: string, page = 0, size = 20) =>
    runAction(token, () => restoreUser(token, username), page, size)

  const unlock = (token: string, username: string, page = 0, size = 20) =>
    runAction(token, () => unlockUser(token, username), page, size)

  const withdraw = (token: string, username: string, page = 0, size = 20) =>
    runAction(token, () => withdrawUserByAdmin(token, username), page, size)

  const changeRole = (token: string, username: string, role: 'USER' | 'ADMIN', page = 0, size = 20) =>
    runAction(token, () => changeUserRole(token, username, role), page, size)

  const remove = (token: string, username: string, page = 0, size = 20) =>
    runAction(token, () => deleteUser(token, username), page, size)

  const resetPwdFail = (token: string, username: string, page = 0, size = 20) =>
    runAction(token, () => resetPasswordFailCount(token, username), page, size)

  const resetLoginFail = (token: string, username: string, page = 0, size = 20) =>
    runAction(token, () => resetLoginFailCount(token, username), page, size)

  const addUser = (
    token: string,
    body: {
      username: string
      password: string
      displayName?: string | null
      role: 'USER' | 'ADMIN'
      activateImmediately: boolean
    },
    page = 0,
    size = 20,
  ) => runAction(token, () => createAdminUser(token, body), page, size)

  const updateProfile = (
    token: string,
    username: string,
    displayName: string | null,
    page = 0,
    size = 20,
  ) => runAction(token, () => updateUserProfileByAdmin(token, username, { displayName }), page, size)

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
    restore,
    unlock,
    withdraw,
    changeRole,
    remove,
    resetPwdFail,
    resetLoginFail,
    addUser,
    updateProfile,
  }
}
