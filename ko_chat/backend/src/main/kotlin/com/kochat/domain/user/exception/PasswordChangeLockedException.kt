package com.kochat.domain.user.exception

class PasswordChangeLockedException :
    RuntimeException("비밀번호 변경 실패 횟수(3회)를 초과했습니다. 관리자에게 문의하세요.")
