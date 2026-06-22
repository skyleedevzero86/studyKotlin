const ROLE_LABELS: Record<string, string> = {
  ADMIN: '관리자',
  USER: '일반 사용자',
}

const STATUS_LABELS: Record<string, string> = {
  PENDING: '승인 대기',
  ACTIVE: '정상 이용',
  WITHDRAWN: '탈퇴',
  SUSPENDED: '이용 정지',
  PASSWORD_LOCKED: '비밀번호 잠금',
}

export const formatRole = (role: string | null | undefined): string =>
  role ? (ROLE_LABELS[role] ?? role) : '-'

export const formatStatus = (status: string | null | undefined): string =>
  status ? (STATUS_LABELS[status] ?? status) : '-'

export const ROLE_OPTIONS = [
  { value: 'USER' as const, label: '일반 사용자' },
  { value: 'ADMIN' as const, label: '관리자' },
]
