import { reactive, ref } from 'vue'
import { join as joinApi } from '../api/joinApi'
import { ApiError } from '../api/http'

export const useJoinForm = () => {
  const form = reactive({
    username: '',
    password: '',
    confirmPassword: '',
    displayName: '',
  })
  const errorMessage = ref<string | null>(null)
  const successMessage = ref<string | null>(null)
  const isLoading = ref(false)

  const submit = async (): Promise<boolean> => {
    errorMessage.value = null
    successMessage.value = null

    if (form.password !== form.confirmPassword) {
      errorMessage.value = '비밀번호 확인이 일치하지 않습니다.'
      return false
    }

    isLoading.value = true
    try {
      const response = await joinApi({
        username: form.username,
        password: form.password,
        displayName: form.displayName.trim() || null,
      })
      successMessage.value = `${response.message} (아이디: ${response.username})`
      return true
    } catch (error) {
      errorMessage.value = error instanceof ApiError ? error.message : '회원가입에 실패했습니다.'
      return false
    } finally {
      isLoading.value = false
    }
  }

  return { form, errorMessage, successMessage, isLoading, submit }
}
