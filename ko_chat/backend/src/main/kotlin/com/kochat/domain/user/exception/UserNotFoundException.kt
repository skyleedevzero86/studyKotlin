package com.kochat.domain.user.exception

class UserNotFoundException(username: String) :
    RuntimeException("사용자를 찾을 수 없습니다: $username")
