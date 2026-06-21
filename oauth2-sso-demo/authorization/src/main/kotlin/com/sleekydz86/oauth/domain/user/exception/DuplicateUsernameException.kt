package com.sleekydz86.oauth.domain.user.exception

class DuplicateUsernameException(username: String) :
    RuntimeException("이미 사용 중인 아이디입니다: $username")
