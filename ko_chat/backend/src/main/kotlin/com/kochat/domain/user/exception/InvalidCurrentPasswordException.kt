package com.kochat.domain.user.exception

class InvalidCurrentPasswordException :
    RuntimeException("현재 비밀번호가 일치하지 않습니다.")
