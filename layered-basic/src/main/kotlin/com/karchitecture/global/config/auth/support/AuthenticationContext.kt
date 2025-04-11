package com.karchitecture.global.config.auth.support

import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope

@RequestScope
@Component
class AuthenticationContext(
    private var memberId: Long? = null
) {

    fun setAuthentication(memberId: Long) {
        this.memberId = memberId
    }

    fun getPrincipal(): Long {
        return memberId
            ?: throw IllegalArgumentException("로그인 정보를 찾을 수 없습니다.")
    }

    fun setAnonymous() {
        this.memberId = ANONYMOUS_MEMBER
    }

    companion object {
        private const val ANONYMOUS_MEMBER = -1L
    }
}