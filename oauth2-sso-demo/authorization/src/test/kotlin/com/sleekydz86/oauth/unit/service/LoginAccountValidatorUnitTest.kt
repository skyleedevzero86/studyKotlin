package com.sleekydz86.oauth.unit.service

import com.sleekydz86.oauth.domain.user.exception.LoginDeniedException
import com.sleekydz86.oauth.domain.user.exception.PasswordChangeRequiredException
import com.sleekydz86.oauth.domain.user.model.User
import com.sleekydz86.oauth.domain.user.model.UserRole
import com.sleekydz86.oauth.domain.user.model.UserStatus
import com.sleekydz86.oauth.global.security.login.LoginAccountValidator
import com.sleekydz86.oauth.support.InMemoryUserPersistencePort
import com.sleekydz86.oauth.support.PlainPasswordEncoderPort
import com.sleekydz86.oauth.support.TestLog
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.time.temporal.ChronoUnit

@DisplayName("LoginAccountValidator 단위 테스트 - 로그인 전 계정 상태 검증 역할")
class LoginAccountValidatorUnitTest {

    private lateinit var persistencePort: InMemoryUserPersistencePort
    private lateinit var validator: LoginAccountValidator

    @BeforeEach
    fun setUp() {
        persistencePort = InMemoryUserPersistencePort()
        validator = LoginAccountValidator(persistencePort)
    }

    @Test
    @DisplayName("역할: 로그인 검증 - ACTIVE 회원은 통과한다")
    fun validateActiveUser() {
        val name = "로그인 검증 - ACTIVE 통과"
        TestLog.start(name)

        // given
        TestLog.given("LoginAccountValidator", "ACTIVE newbie 저장")
        saveActiveUser("newbie")

        // when
        TestLog.`when`("validate", "newbie 검증")
        validator.validate("newbie")

        // then
        TestLog.then("validate", "예외 없이 통과")

        TestLog.end(name)
    }

    @Test
    @DisplayName("역할: 로그인 검증 - PENDING 회원은 승인 대기 메시지로 거부한다")
    fun validatePendingUser() {
        val name = "로그인 검증 - PENDING 거부"
        TestLog.start(name)

        // given
        TestLog.given("LoginAccountValidator", "PENDING newbie 저장")
        val encoder = PlainPasswordEncoderPort()
        persistencePort.save(User.createPending("newbie", encoder.encode("pass1234")))

        // when / then
        TestLog.`when`("validate", "PENDING newbie 검증")
        TestLog.then("validate", "LoginDeniedException(관리자 승인 후...)")
        assertThrows<LoginDeniedException> {
            validator.validate("newbie")
        }

        TestLog.end(name)
    }

    @Test
    @DisplayName("역할: 로그인 검증 - 비밀번호 30일 만료 시 PasswordChangeRequiredException")
    fun validateExpiredPassword() {
        val name = "로그인 검증 - 비밀번호 만료"
        TestLog.start(name)

        // given
        val oldChangedAt = Instant.now().minus(31, ChronoUnit.DAYS)
        TestLog.given("LoginAccountValidator", "ACTIVE newbie, passwordChangedAt 31일 전")
        persistencePort.save(
            User.restore(
                id = 1L,
                username = "newbie",
                password = "encoded:pass",
                displayName = null,
                role = UserRole.USER,
                status = UserStatus.ACTIVE,
                createdAt = oldChangedAt,
                passwordChangedAt = oldChangedAt,
                passwordChangeFailCount = 0,
                loginFailCount = 0,
                lastLoginAt = null,
            ),
        )

        // when / then
        TestLog.`when`("validate", "만료된 newbie 검증")
        TestLog.then("validate", "PasswordChangeRequiredException 발생")
        assertThrows<PasswordChangeRequiredException> {
            validator.validate("newbie")
        }

        TestLog.end(name)
    }

    @Test
    @DisplayName("역할: 로그인 검증 - PASSWORD_LOCKED 회원은 거부한다")
    fun validatePasswordLockedUser() {
        val name = "로그인 검증 - PASSWORD_LOCKED 거부"
        TestLog.start(name)

        // given
        TestLog.given("LoginAccountValidator", "PASSWORD_LOCKED newbie")
        persistencePort.save(
            User.createActiveAdmin("newbie", "encoded:pass").withPasswordLocked(),
        )

        // when / then
        TestLog.`when`("validate", "잠금 계정 검증")
        TestLog.then("validate", "LoginDeniedException 발생")
        assertThrows<LoginDeniedException> {
            validator.validate("newbie")
        }

        TestLog.end(name)
    }

    private fun saveActiveUser(username: String) {
        val encoder = PlainPasswordEncoderPort()
        persistencePort.save(User.createPending(username, encoder.encode("pass1234")))
        persistencePort.save(
            persistencePort.findByUsername(username)!!
                .withStatus(UserStatus.ACTIVE),
        )
    }
}
