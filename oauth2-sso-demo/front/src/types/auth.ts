export type LoginRequest = {
  username: string
  password: string
}

export type LoginResponse = {
  accessToken: string
}

export type ApiErrorResponse = {
  error?: string
  code?: string
}
