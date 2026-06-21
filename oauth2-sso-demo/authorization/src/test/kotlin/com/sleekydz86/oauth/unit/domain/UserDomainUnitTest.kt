package com.sleekydz86.oauth.unit.domain

import com.sleekydz86.oauth.domain.user.model.User
import com.sleekydz86.oauth.domain.user.model.UserRole
import com.sleekydz86.oauth.domain.user.model.UserStatus
import com.sleekydz86.oauth.support.TestLog
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("User 도메인 모델 단위 테스트 - 회원 상태·비밀번호 정책 역할")
class UserDomainUnitTest {

    @Test
    @DisplayName("역할: 신규 가입 - PENDING 상태와 USER 권한으로 생성된다")
    fun createPendingUser() {
        val name = "역할: 신규 가입 - PENDING 상태와 USER 권한으로 생성된다"
        TestLog.start(name)

        // given
        TestLog.given("User.createPending", "username=user1, encodedPassword=encoded:pass")
        val now = Instant.parse("2026-01-01T00:00:00Z")

        // when
        TestLog.`when`("User.createPending", "대기 상태 회원 객체 생성")
        val user = User.createPending("user1", "encoded:pass", now = now)

        // then
        TestLog.then("User.createPending", "status=PENDING, role=USER, canLogin=false")
        assertEquals(UserStatus.PENDING, user.status)
        assertEquals(UserRole.USER, user.role)
        assertFalse(user.canLogin())

        TestLog.end(name)
    }

    @Test
    @DisplayName("역할: 관리자 부트스트랩 - ACTIVE 상태와 ADMIN 권한으로 생성된다")
    fun createActiveAdmin() {
        val name = "역할: 관리자 부트스트랩 - ACTIVE 상태와 ADMIN 권한으로 생성된다"
        TestLog.start(name)

        // given
        TestLog.given("User.createActiveAdmin", "username=admin")

        // when
        TestLog.`when`("User.createActiveAdmin", "활성 관리자 객체 생성")
        val admin = User.createActiveAdmin("admin", "encoded:admin1234")

        // then
        TestLog.then("User.createActiveAdmin", "status=ACTIVE, role=ADMIN, canLogin=true")
        assertEquals(UserStatus.ACTIVE, admin.status)
        assertEquals(UserRole.ADMIN, admin.role)
        assertTrue(admin.canLogin())

        TestLog.end(name)
    }

    @Test
    @DisplayName("역할: 비밀번호 정책 - 30일 경과 시 만료로 판단한다")
    fun passwordExpiredAfter30Days() {
        val name = "역할: 비밀번호 정책 - 30일 경과 시 만료로 판단한다"
        TestLog.start(name)

        // given
        val changedAt = Instant.parse("2026-01-01T00:00:00Z")
        val now = changedAt.plus(31, ChronoUnit.DAYS)
        TestLog.given("User", "passwordChangedAt=$changedAt, now=$now (31일 경과)")

        val user = User.createActiveAdmin("user1", "encoded:pass", now = changedAt)

        // when
        TestLog.`when`("User.isPasswordExpired", "만료 여부 확인")
        val expired = user.isPasswordExpired(now)

        // then
        TestLog.then("User.isPasswordExpired", "true 반환")
        assertTrue(expired)

        TestLog.end(name)
    }

    @Test
    @DisplayName("역할: 비밀번호 변경 - 성공 시 실패 횟수가 0으로 초기화된다")
    fun passwordChangeResetsFailCount() {
        val name = "역할: 비밀번호 변경 - 성공 시 실패 횟수가 0으로 초기화된다"
        TestLog.start(name)

        // given
        TestLog.given("User", "passwordChangeFailCount=2 상태")
        val user = User.createActiveAdmin("user1", "encoded:old")
            .withPasswordChangeFailCount(2)

        // when
        TestLog.`when`("User.withPassword", "새 비밀번호로 변경")
        val updated = user.withPassword("encoded:new", Instant.now())

        // then
        TestLog.then("User.withPassword", "failCount=0, password 갱신")
        assertEquals(0, updated.passwordChangeFailCount)
        assertEquals("encoded:new", updated.password)

        TestLog.end(name)
    }

    @Test
    @DisplayName("역할: 비밀번호 잠금 - 3회 실패 시 PASSWORD_LOCKED 상태가 된다")
    fun passwordLockedStatus() {
        val name = "역할: 비밀번호 잠금 - 3회 실패 시 PASSWORD_LOCKED 상태가 된다"
        TestLog.start(name)

        // given
        TestLog.given("User", "ACTIVE 상태 회원")
        val user = User.createActiveAdmin("user1", "encoded:pass")

        // when
        TestLog.`when`("User.withPasswordLocked", "잠금 처리")
        val locked = user.withPasswordLocked()

        // then
        TestLog.then("User.withPasswordLocked", "status=PASSWORD_LOCKED, failCount=3")
        assertEquals(UserStatus.PASSWORD_LOCKED, locked.status)
        assertEquals(User.MAX_PASSWORD_CHANGE_FAILS, locked.passwordChangeFailCount)
        assertFalse(locked.canLogin())

        TestLog.end(name)
    }
}
