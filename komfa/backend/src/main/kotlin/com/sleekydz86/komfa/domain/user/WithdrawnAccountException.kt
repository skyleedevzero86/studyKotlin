package com.sleekydz86.komfa.domain.user

class WithdrawnAccountException(message: String = "탈퇴한 계정입니다. 관리자에게 문의하세요.") : IllegalStateException(message)
