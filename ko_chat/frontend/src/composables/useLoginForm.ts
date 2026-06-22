import { reactive } from 'vue'
import type { LoginRequest } from '../types/auth'
import { useAuth } from './useAuth'

export const useLoginForm = () => {
  const { login, errorMessage, isLoading, clearError } = useAuth()

  const form = reactive<LoginRequest>({
    username: '',
    password: '',
  })

  const submit = async (): Promise<boolean> => {
    clearError()
    return login({
      username: form.username.trim(),
      password: form.password,
    })
  }

  return {
    form,
    errorMessage,
    isLoading,
    submit,
  }
}
