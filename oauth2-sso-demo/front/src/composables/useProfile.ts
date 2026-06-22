import { reactive, ref } from 'vue'
import {
  changePassword,
  fetchMyProfile,
  updateProfile,
  withdraw,
} from '../api/userApi'
import { ApiError } from '../api/http'
import type { UserProfileResponse } from '../types/user'

export const useProfile = () => {
  const profile = ref<UserProfileResponse | null>(null)
  const profileForm = reactive({ displayName: '' })
  const passwordForm = reactive({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  })
  const isLoading = ref(false)
  const errorMessage = ref<string | null>(null)
  const successMessage = ref<string | null>(null)

  const loadProfile = async (token: string): Promise<void> => {
    isLoading.value = true
    errorMessage.value = null
    try {
      profile.value = await fetchMyProfile(token)
      profileForm.displayName = profile.value.displayName ?? ''
    } catch (error) {
      errorMessage.value = error instanceof ApiError ? error.message : '프로필을 불러오지 못했습니다.'
    } finally {
      isLoading.value = false
    }
  }

  const saveProfile = async (token: string): Promise<boolean> => {
    errorMessage.value = null
    successMessage.value = null
    isLoading.value = true
    try {
      const result = await updateProfile(token, {
        displayName: profileForm.displayName.trim() || null,
      })
      successMessage.value = result.message
      await loadProfile(token)
      return true
    } catch (error) {
      errorMessage.value = error instanceof ApiError ? error.message : '프로필 수정에 실패했습니다.'
      return false
    } finally {
      isLoading.value = false
    }
  }

  const savePassword = async (username: string): Promise<boolean> => {
    errorMessage.value = null
    successMessage.value = null

    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      errorMessage.value = '새 비밀번호 확인이 일치하지 않습니다.'
      return false
    }

    isLoading.value = true
    try {
      const result = await changePassword({
        username,
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword,
      })
      successMessage.value = result.message
      passwordForm.currentPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      return true
    } catch (error) {
      errorMessage.value = error instanceof ApiError ? error.message : '비밀번호 변경에 실패했습니다.'
      return false
    } finally {
      isLoading.value = false
    }
  }

  const withdrawAccount = async (token: string): Promise<boolean> => {
    errorMessage.value = null
    successMessage.value = null
    isLoading.value = true
    try {
      const result = await withdraw(token)
      successMessage.value = result.message
      return true
    } catch (error) {
      errorMessage.value = error instanceof ApiError ? error.message : '탈퇴 처리에 실패했습니다.'
      return false
    } finally {
      isLoading.value = false
    }
  }

  return {
    profile,
    profileForm,
    passwordForm,
    isLoading,
    errorMessage,
    successMessage,
    loadProfile,
    saveProfile,
    savePassword,
    withdrawAccount,
  }
}
