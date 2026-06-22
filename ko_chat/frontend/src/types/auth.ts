export type LoginRequest = {
  username: string
  password: string
}

export type LoginResponse = {
  accessToken: string
}

export type JoinRequest = {
  username: string
  password: string
  displayName?: string | null
}

export type JoinResponse = {
  message: string
  status: string
  username: string
}

export type ApiErrorResponse = {
  error?: string
  code?: string
}

export type ApiMessageResponse = {
  message: string
  status?: string
  role?: string
}
