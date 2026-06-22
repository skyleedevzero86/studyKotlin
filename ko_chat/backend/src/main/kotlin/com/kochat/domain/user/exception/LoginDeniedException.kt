package com.kochat.domain.user.exception

class LoginDeniedException(
    message: String,
) : RuntimeException(message)
