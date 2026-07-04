import { ApiError } from '../api/http'

export const resolveApiError = (error: unknown, fallback: string): string => {
  if (error instanceof ApiError) {
    if (error.status === 0) {
      return error.message
    }
    if (error.status === 401) {
      if (error.code === 'EXPIRED_TOKEN') {
        return '로그인이 만료되었습니다. 다시 로그인해 주세요.'
      }
      if (error.code === 'AUTHENTICATION_FAILED' || error.code === 'INVALID_TOKEN') {
        return '인증이 필요합니다. 다시 로그인해 주세요.'
      }
      if (error.code === 'INACTIVE_ACCOUNT') {
        return '비활성화된 계정입니다. 관리자에게 문의해 주세요.'
      }
      if (error.code === 'PASSWORD_EXPIRED') {
        return '비밀번호 변경이 필요합니다. 다시 로그인해 주세요.'
      }
      return '인증에 실패했습니다. 다시 로그인해 주세요.'
    }
    if (error.status === 404) {
      return '요청한 API를 찾을 수 없습니다. 백엔드가 실행 중인지, 관련 서비스(Kafka 등)가 활성화되어 있는지 확인해 주세요.'
    }
    return error.message
  }
  if (error instanceof Error) return error.message
  return fallback
}
