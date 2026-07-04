package com.kochat.global.security.login

import com.kochat.domain.user.exception.LoginDeniedException
import com.kochat.domain.user.exception.PasswordChangeRequiredException
import com.kochat.domain.user.model.User
import com.kochat.domain.user.model.UserStatus
import com.kochat.domain.user.port.out.UserPersistencePort
import org.springframework.stereotype.Component

@Component
class LoginAccountValidator(
    private val userPersistencePort: UserPersistencePort,
) {

    fun validate(username: String) {
        val user = userPersistencePort.findByUsername(username)
            ?: throw LoginDeniedException("아이디 또는 비밀번호가 올바르지 않습니다.")

        when (user.status) {
            UserStatus.PENDING ->
                throw LoginDeniedException("관리자 승인 후 이용 가능합니다.")

            UserStatus.WITHDRAWN ->
                throw LoginDeniedException("탈퇴 처리된 계정입니다. 관리자에게 문의하세요.")

            UserStatus.SUSPENDED ->
                throw LoginDeniedException("이용이 정지된 계정입니다. 관리자에게 문의하세요.")

            UserStatus.PASSWORD_LOCKED ->
                throw LoginDeniedException("비밀번호 변경 실패 횟수(3회)를 초과했습니다. 관리자에게 문의하세요.")

            UserStatus.LOGIN_LOCKED ->
                throw LoginDeniedException("로그인 실패 횟수(3회)를 초과하여 계정이 잠겼습니다. 관리자에게 문의하세요.")

            UserStatus.ACTIVE -> {
                if (user.isPasswordExpired()) {
                    throw PasswordChangeRequiredException()
                }
            }
        }
    }

    fun findUser(username: String): User =
        userPersistencePort.findByUsername(username)
            ?: throw LoginDeniedException("아이디 또는 비밀번호가 올바르지 않습니다.")
}
