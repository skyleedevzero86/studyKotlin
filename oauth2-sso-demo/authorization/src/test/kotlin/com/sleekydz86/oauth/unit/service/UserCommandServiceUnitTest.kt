package com.sleekydz86.oauth.unit.service

import com.sleekydz86.oauth.domain.user.exception.DuplicateUsernameException
import com.sleekydz86.oauth.domain.user.exception.InvalidCurrentPasswordException
import com.sleekydz86.oauth.domain.user.exception.InvalidUserStatusException
import com.sleekydz86.oauth.domain.user.exception.PasswordChangeLockedException
import com.sleekydz86.oauth.domain.user.exception.UserNotFoundException
import com.sleekydz86.oauth.domain.user.model.ApproveUserCommand
import com.sleekydz86.oauth.domain.user.model.ChangePasswordWithVerifyCommand
import com.sleekydz86.oauth.domain.user.model.JoinCommand
import com.sleekydz86.oauth.domain.user.model.RestoreUserCommand
import com.sleekydz86.oauth.domain.user.model.SuspendUserCommand
import com.sleekydz86.oauth.domain.user.model.UserRole
import com.sleekydz86.oauth.domain.user.model.UserStatus
import com.sleekydz86.oauth.domain.user.model.WithdrawUserCommand
import com.sleekydz86.oauth.domain.user.service.UserCommandService
import com.sleekydz86.oauth.support.InMemoryUserPersistencePort
import com.sleekydz86.oauth.support.PlainPasswordEncoderPort
import com.sleekydz86.oauth.support.TestLog
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DisplayName("UserCommandService 단위 테스트 - 회원 CUD 도메인 서비스 역할")
class UserCommandServiceUnitTest {

    private lateinit var persistencePort: InMemoryUserPersistencePort
    private lateinit var passwordEncoderPort: PlainPasswordEncoderPort
    private lateinit var userCommandService: UserCommandService

    @BeforeEach
    fun setUp() {
        persistencePort = InMemoryUserPersistencePort()
        passwordEncoderPort = PlainPasswordEncoderPort()
        userCommandService = UserCommandService(persistencePort, passwordEncoderPort)
    }

    @Nested
    @DisplayName("join 기능")
    inner class JoinTests {

        @Test
        @DisplayName("역할: 회원 가입 - 신규 사용자를 PENDING 상태로 저장한다")
        fun joinSuccess() {
            val name = "join - 신규 사용자 PENDING 저장"
            TestLog.start(name)

            // given
            TestLog.given("UserCommandService", "username=newbie, 중복 없음")

            // when
            TestLog.`when`("join", "JoinCommand 실행")
            val user = userCommandService.join(JoinCommand("newbie", "pass1234"))

            // then
            TestLog.then("join", "status=PENDING, username=newbie")
            assertEquals("newbie", user.username)
            assertEquals(UserStatus.PENDING, user.status)

            TestLog.end(name)
        }

        @Test
        @DisplayName("역할: 회원 가입 - 중복 아이디면 DuplicateUsernameException")
        fun joinDuplicate() {
            val name = "join - 중복 아이디 예외"
            TestLog.start(name)

            // given
            TestLog.given("UserCommandService", "username=dup 사용자 이미 존재")
            userCommandService.join(JoinCommand("dup", "pass1234"))

            // when / then
            TestLog.`when`("join", "동일 username 재가입 시도")
            TestLog.then("join", "DuplicateUsernameException 발생")
            assertThrows<DuplicateUsernameException> {
                userCommandService.join(JoinCommand("dup", "other"))
            }

            TestLog.end(name)
        }
    }

    @Nested
    @DisplayName("approve 기능")
    inner class ApproveTests {

        @Test
        @DisplayName("역할: 관리자 승인 - PENDING 회원을 ACTIVE로 전환한다")
        fun approvePendingUser() {
            val name = "approve - PENDING → ACTIVE"
            TestLog.start(name)

            // given
            TestLog.given("UserCommandService", "PENDING 상태 newbie")
            userCommandService.join(JoinCommand("newbie", "pass1234"))

            // when
            TestLog.`when`("approve", "ApproveUserCommand(USER) 실행")
            val approved = userCommandService.approve(ApproveUserCommand("newbie", UserRole.USER))

            // then
            TestLog.then("approve", "status=ACTIVE")
            assertEquals(UserStatus.ACTIVE, approved.status)

            TestLog.end(name)
        }

        @Test
        @DisplayName("역할: 관리자 승인 - ACTIVE 회원은 승인할 수 없다")
        fun approveAlreadyActive() {
            val name = "approve - ACTIVE 회원 승인 불가"
            TestLog.start(name)

            // given
            TestLog.given("UserCommandService", "이미 ACTIVE인 newbie")
            userCommandService.join(JoinCommand("newbie", "pass1234"))
            userCommandService.approve(ApproveUserCommand("newbie", UserRole.USER))

            // when / then
            TestLog.`when`("approve", "ACTIVE 회원 재승인 시도")
            TestLog.then("approve", "InvalidUserStatusException 발생")
            assertThrows<InvalidUserStatusException> {
                userCommandService.approve(ApproveUserCommand("newbie", UserRole.USER))
            }

            TestLog.end(name)
        }
    }

    @Nested
    @DisplayName("suspend / withdraw 기능")
    inner class LifecycleTests {

        @Test
        @DisplayName("역할: 이용 정지 - ACTIVE 회원을 SUSPENDED로 변경한다")
        fun suspendActiveUser() {
            val name = "suspend - ACTIVE → SUSPENDED"
            TestLog.start(name)

            // given
            TestLog.given("UserCommandService", "ACTIVE newbie")
            userCommandService.join(JoinCommand("newbie", "pass1234"))
            userCommandService.approve(ApproveUserCommand("newbie", UserRole.USER))

            // when
            TestLog.`when`("suspend", "SuspendUserCommand 실행")
            val suspended = userCommandService.suspend(SuspendUserCommand("newbie"))

            // then
            TestLog.then("suspend", "status=SUSPENDED")
            assertEquals(UserStatus.SUSPENDED, suspended.status)

            TestLog.end(name)
        }

        @Test
        @DisplayName("역할: 회원 탈퇴 - WITHDRAWN 상태로 변경한다")
        fun withdrawUser() {
            val name = "withdraw - WITHDRAWN 처리"
            TestLog.start(name)

            // given
            TestLog.given("UserCommandService", "ACTIVE newbie")
            userCommandService.join(JoinCommand("newbie", "pass1234"))
            userCommandService.approve(ApproveUserCommand("newbie", UserRole.USER))

            // when
            TestLog.`when`("withdraw", "WithdrawUserCommand 실행")
            val withdrawn = userCommandService.withdraw(WithdrawUserCommand("newbie"))

            // then
            TestLog.then("withdraw", "status=WITHDRAWN")
            assertEquals(UserStatus.WITHDRAWN, withdrawn.status)

            TestLog.end(name)
        }
    }

    @Nested
    @DisplayName("restore 기능")
    inner class RestoreTests {

        @Test
        @DisplayName("역할: 회원 복구 - SUSPENDED 회원을 ACTIVE로 복구한다")
        fun restoreSuspendedUser() {
            val name = "restore - SUSPENDED → ACTIVE"
            TestLog.start(name)

            TestLog.given("UserCommandService", "SUSPENDED newbie")
            userCommandService.join(JoinCommand("newbie", "pass1234"))
            userCommandService.approve(ApproveUserCommand("newbie", UserRole.USER))
            userCommandService.suspend(SuspendUserCommand("newbie"))

            TestLog.`when`("restore", "RestoreUserCommand 실행")
            val restored = userCommandService.restore(RestoreUserCommand("newbie"))

            TestLog.then("restore", "status=ACTIVE, fail counts reset")
            assertEquals(UserStatus.ACTIVE, restored.status)
            assertEquals(0, restored.loginFailCount)
            assertEquals(0, restored.passwordChangeFailCount)

            TestLog.end(name)
        }

        @Test
        @DisplayName("역할: 회원 복구 - WITHDRAWN 회원을 ACTIVE로 복구한다")
        fun restoreWithdrawnUser() {
            val name = "restore - WITHDRAWN → ACTIVE"
            TestLog.start(name)

            TestLog.given("UserCommandService", "WITHDRAWN newbie")
            userCommandService.join(JoinCommand("newbie", "pass1234"))
            userCommandService.approve(ApproveUserCommand("newbie", UserRole.USER))
            userCommandService.withdraw(WithdrawUserCommand("newbie"))

            TestLog.`when`("restore", "RestoreUserCommand 실행")
            val restored = userCommandService.restore(RestoreUserCommand("newbie"))

            TestLog.then("restore", "status=ACTIVE")
            assertEquals(UserStatus.ACTIVE, restored.status)

            TestLog.end(name)
        }

        @Test
        @DisplayName("역할: 회원 복구 - ACTIVE 회원은 복구할 수 없다")
        fun cannotRestoreActiveUser() {
            val name = "restore - ACTIVE 거부"
            TestLog.start(name)

            TestLog.given("UserCommandService", "ACTIVE newbie")
            userCommandService.join(JoinCommand("newbie", "pass1234"))
            userCommandService.approve(ApproveUserCommand("newbie", UserRole.USER))

            TestLog.`when`("restore", "ACTIVE 회원 복구 시도")
            TestLog.then("restore", "InvalidUserStatusException 발생")
            assertThrows<InvalidUserStatusException> {
                userCommandService.restore(RestoreUserCommand("newbie"))
            }

            TestLog.end(name)
        }
    }

    @Nested
    @DisplayName("changePassword 기능")
    inner class ChangePasswordTests {

        @Test
        @DisplayName("역할: 비밀번호 변경 - 현재 비밀번호가 맞으면 변경된다")
        fun changePasswordSuccess() {
            val name = "changePassword - 성공"
            TestLog.start(name)

            // given
            TestLog.given("UserCommandService", "ACTIVE newbie, password=pass1234")
            userCommandService.join(JoinCommand("newbie", "pass1234"))
            userCommandService.approve(ApproveUserCommand("newbie", UserRole.USER))

            // when
            TestLog.`when`("changePassword", "current=pass1234, new=newpass5678")
            val updated = userCommandService.changePassword(
                ChangePasswordWithVerifyCommand("newbie", "pass1234", "newpass5678"),
            )

            // then
            TestLog.then("changePassword", "password 갱신, failCount=0")
            assertTrue(passwordEncoderPort.matches("newpass5678", updated.password))
            assertEquals(0, updated.passwordChangeFailCount)

            TestLog.end(name)
        }

        @Test
        @DisplayName("역할: 비밀번호 변경 - 3회 실패 시 PASSWORD_LOCKED")
        fun changePasswordLockAfterThreeFailures() {
            val name = "changePassword - 3회 실패 잠금"
            TestLog.start(name)

            // given
            TestLog.given("UserCommandService", "ACTIVE newbie")
            userCommandService.join(JoinCommand("newbie", "pass1234"))
            userCommandService.approve(ApproveUserCommand("newbie", UserRole.USER))

            // when
            TestLog.`when`("changePassword", "잘못된 현재 비밀번호 3회 입력")
            repeat(2) {
                assertThrows<InvalidCurrentPasswordException> {
                    userCommandService.changePassword(
                        ChangePasswordWithVerifyCommand("newbie", "wrong", "newpass5678"),
                    )
                }
            }

            // then
            TestLog.then("changePassword", "3회째 PasswordChangeLockedException, status=PASSWORD_LOCKED")
            assertThrows<PasswordChangeLockedException> {
                userCommandService.changePassword(
                    ChangePasswordWithVerifyCommand("newbie", "wrong", "newpass5678"),
                )
            }
            assertEquals(UserStatus.PASSWORD_LOCKED, persistencePort.findByUsername("newbie")!!.status)

            TestLog.end(name)
        }
    }

    @Nested
    @DisplayName("delete 기능")
    inner class DeleteTests {

        @Test
        @DisplayName("역할: 회원 삭제 - 존재하지 않으면 UserNotFoundException")
        fun deleteNotFound() {
            val name = "delete - 미존재 회원"
            TestLog.start(name)

            // given
            TestLog.given("UserCommandService", "username=ghost 미존재")

            // when / then
            TestLog.`when`("delete", "DeleteUserCommand(ghost) 실행")
            TestLog.then("delete", "UserNotFoundException 발생")
            assertThrows<UserNotFoundException> {
                userCommandService.delete(com.sleekydz86.oauth.domain.user.model.DeleteUserCommand("ghost"))
            }

            TestLog.end(name)
        }
    }
}
