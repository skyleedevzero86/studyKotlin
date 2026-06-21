package com.sleekydz86.oauth.domain.user.exception

class UserNotFoundException(username: String) :
    RuntimeException("사용자를 찾을 수 없습니다: $username")
