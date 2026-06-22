import { computed, ref } from 'vue'
import { login as loginApi } from '../api/authApi'
import { ApiError } from '../api/http'
import type { LoginRequest } from '../types/auth'
import { isAdminRole, parseJwtRole, parseJwtSubject } from '../utils/crypto'

const TOKEN_KEY = 'accessToken'

const accessToken = ref<string | null>(localStorage.getItem(TOKEN_KEY))
const errorMessage = ref<string | null>(null)
const isLoading = ref(false)

const persistToken = (token: string | null): void => {
  accessToken.value = token
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
    return
  }
  localStorage.removeItem(TOKEN_KEY)
}

const resolveLoginError = (error: unknown): string => {
  if (error instanceof ApiError) {
    return error.message
  }
  if (error instanceof Error) {
    return error.message
  }
  return '로그인에 실패했습니다.'
}

export const useAuth = () => {
  const isAuthenticated = computed(() => Boolean(accessToken.value))
  const role = computed(() =>
    accessToken.value ? parseJwtRole(accessToken.value) : null,
  )
  const username = computed(() =>
    accessToken.value ? parseJwtSubject(accessToken.value) : null,
  )
  const isAdmin = computed(() => isAdminRole(role.value))

  const login = async (credentials: LoginRequest): Promise<boolean> => {
    isLoading.value = true
    errorMessage.value = null

    try {
      const response = await loginApi(credentials)
      persistToken(response.accessToken)
      return true
    } catch (error) {
      errorMessage.value = resolveLoginError(error)
      return false
    } finally {
      isLoading.value = false
    }
  }

  const logout = (): void => {
    persistToken(null)
    errorMessage.value = null
  }

  const clearError = (): void => {
    errorMessage.value = null
  }

  return {
    accessToken,
    errorMessage,
    isLoading,
    isAuthenticated,
    role,
    username,
    isAdmin,
    login,
    logout,
    clearError,
  }
}
