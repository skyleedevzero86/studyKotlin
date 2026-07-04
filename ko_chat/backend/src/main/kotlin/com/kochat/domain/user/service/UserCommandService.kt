package com.kochat.domain.user.service

import com.kochat.domain.user.exception.DuplicateUsernameException
import com.kochat.domain.user.exception.InvalidCurrentPasswordException
import com.kochat.domain.user.exception.InvalidUserStatusException
import com.kochat.domain.user.exception.PasswordChangeLockedException
import com.kochat.domain.user.exception.UserNotFoundException
import com.kochat.domain.user.model.ActivateUserCommand
import com.kochat.domain.user.model.ApproveUserCommand
import com.kochat.domain.user.model.ChangePasswordWithVerifyCommand
import com.kochat.domain.user.model.ChangeUserRoleCommand
import com.kochat.domain.user.model.CreateUserByAdminCommand
import com.kochat.domain.user.model.DeleteUserCommand
import com.kochat.domain.user.model.JoinCommand
import com.kochat.domain.user.model.RecordLoginCommand
import com.kochat.domain.user.model.RecordLoginFailureCommand
import com.kochat.domain.user.model.ResetLoginFailCountCommand
import com.kochat.domain.user.model.ResetPasswordFailCountCommand
import com.kochat.domain.user.model.RestoreUserCommand
import com.kochat.domain.user.model.SuspendUserCommand
import com.kochat.domain.user.model.UnlockUserCommand
import com.kochat.domain.user.model.UpdateProfileCommand
import com.kochat.domain.user.model.User
import com.kochat.domain.user.model.UserStatus
import com.kochat.domain.user.model.WithdrawUserCommand
import com.kochat.domain.user.port.out.PasswordEncoderPort
import com.kochat.domain.user.port.out.UserPersistencePort
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class UserCommandService(
    private val userPersistencePort: UserPersistencePort,
    private val passwordEncoderPort: PasswordEncoderPort,
) {

    fun join(command: JoinCommand): User {
        if (userPersistencePort.existsByUsername(command.username)) {
            throw DuplicateUsernameException(command.username)
        }

        val user = User.createPending(
            username = command.username,
            encodedPassword = passwordEncoderPort.encode(command.password),
            displayName = command.displayName,
        )
        return userPersistencePort.save(user)
    }

    fun createByAdmin(command: CreateUserByAdminCommand): User {
        if (userPersistencePort.existsByUsername(command.username)) {
            throw DuplicateUsernameException(command.username)
        }

        val user = User.createByAdmin(
            username = command.username,
            encodedPassword = passwordEncoderPort.encode(command.password),
            role = command.role,
            displayName = command.displayName,
            activateImmediately = command.activateImmediately,
        )
        return userPersistencePort.save(user)
    }

    fun createBootstrapAdmin(username: String, rawPassword: String): User {
        if (userPersistencePort.existsByUsername(username)) {
            return userPersistencePort.findByUsername(username)!!
        }
        val admin = User.createActiveAdmin(
            username = username,
            encodedPassword = passwordEncoderPort.encode(rawPassword),
        )
        return userPersistencePort.save(admin)
    }

    fun updateProfile(command: UpdateProfileCommand): User {
        val user = findUser(command.username)
        if (user.status == UserStatus.WITHDRAWN) {
            throw InvalidUserStatusException("탈퇴 상태에서는 프로필을 수정할 수 없습니다.")
        }
        return userPersistencePort.save(user.withDisplayName(command.displayName))
    }

    fun approve(command: ApproveUserCommand): User {
        val user = findUser(command.username)
        requireStatus(user, UserStatus.PENDING, "승인 대기 상태의 회원만 승인할 수 있습니다.")
        return userPersistencePort.save(user.withStatus(UserStatus.ACTIVE).withRole(command.role))
    }

    fun suspend(command: SuspendUserCommand): User {
        val user = findUser(command.username)
        requireStatus(user, UserStatus.ACTIVE, "활성 상태의 회원만 정지할 수 있습니다.")
        return userPersistencePort.save(user.withStatus(UserStatus.SUSPENDED))
    }

    fun activate(command: ActivateUserCommand): User {
        val user = findUser(command.username)
        if (user.status != UserStatus.SUSPENDED && user.status != UserStatus.PASSWORD_LOCKED && user.status != UserStatus.LOGIN_LOCKED) {
            throw InvalidUserStatusException("정지 또는 잠금 상태의 회원만 활성화할 수 있습니다.")
        }
        return userPersistencePort.save(
            user.withStatus(UserStatus.ACTIVE).withPasswordChangeFailCount(0).withLoginFailCount(0),
        )
    }

    fun restore(command: RestoreUserCommand): User {
        val user = findUser(command.username)
        if (user.status != UserStatus.WITHDRAWN && user.status != UserStatus.SUSPENDED) {
            throw InvalidUserStatusException("탈퇴 또는 정지 상태의 회원만 복구할 수 있습니다.")
        }
        return userPersistencePort.save(
            user.withStatus(UserStatus.ACTIVE)
                .withPasswordChangeFailCount(0)
                .withLoginFailCount(0),
        )
    }

    fun withdraw(command: WithdrawUserCommand): User {
        val user = findUser(command.username)
        if (user.status == UserStatus.WITHDRAWN) {
            throw InvalidUserStatusException("이미 탈퇴 처리된 회원입니다.")
        }
        return userPersistencePort.save(user.withStatus(UserStatus.WITHDRAWN))
    }

    fun changeRole(command: ChangeUserRoleCommand): User {
        val user = findUser(command.username)
        return userPersistencePort.save(user.withRole(command.role))
    }

    fun unlock(command: UnlockUserCommand): User {
        val user = findUser(command.username)
        if (user.status != UserStatus.PASSWORD_LOCKED && user.status != UserStatus.LOGIN_LOCKED) {
            throw InvalidUserStatusException("잠금 상태의 회원만 잠금 해제할 수 있습니다.")
        }
        return userPersistencePort.save(
            user.withStatus(UserStatus.ACTIVE).withPasswordChangeFailCount(0).withLoginFailCount(0),
        )
    }

    fun resetPasswordFailCount(command: ResetPasswordFailCountCommand): User {
        val user = findUser(command.username)
        return userPersistencePort.save(user.withPasswordChangeFailCount(0))
    }

    fun resetLoginFailCount(command: ResetLoginFailCountCommand): User {
        val user = findUser(command.username)
        return userPersistencePort.save(user.withLoginFailCount(0))
    }

    fun changePassword(command: ChangePasswordWithVerifyCommand): User {
        val user = findUser(command.username)
        if (user.status == UserStatus.PASSWORD_LOCKED) {
            throw PasswordChangeLockedException()
        }
        if (user.status == UserStatus.WITHDRAWN || user.status == UserStatus.SUSPENDED) {
            throw InvalidUserStatusException("탈퇴 또는 정지 상태에서는 비밀번호를 변경할 수 없습니다.")
        }

        if (!passwordEncoderPort.matches(command.currentPassword, user.password)) {
            val failCount = user.passwordChangeFailCount + 1
            if (failCount >= User.MAX_PASSWORD_CHANGE_FAILS) {
                userPersistencePort.save(user.withPasswordLocked())
                throw PasswordChangeLockedException()
            }
            userPersistencePort.save(user.withPasswordChangeFailCount(failCount))
            throw InvalidCurrentPasswordException()
        }

        return userPersistencePort.save(
            user.withPassword(
                encodedPassword = passwordEncoderPort.encode(command.newPassword),
                changedAt = Instant.now(),
            ),
        )
    }

    fun recordLogin(command: RecordLoginCommand): User {
        val user = findUser(command.username)
        return userPersistencePort.save(user.withLastLoginAt(Instant.now()))
    }

    fun recordLoginFailure(command: RecordLoginFailureCommand): User? {
        val user = userPersistencePort.findByUsername(command.username) ?: return null
        val newCount = user.loginFailCount + 1
        val updated = if (newCount >= User.MAX_LOGIN_FAILS) {
            user.withLoginLocked()
        } else {
            user.withLoginFailCount(newCount)
        }
        return userPersistencePort.save(updated)
    }

    fun delete(command: DeleteUserCommand) {
        if (!userPersistencePort.existsByUsername(command.username)) {
            throw UserNotFoundException(command.username)
        }
        userPersistencePort.deleteByUsername(command.username)
    }

    private fun findUser(username: String): User =
        userPersistencePort.findByUsername(username) ?: throw UserNotFoundException(username)

    private fun requireStatus(user: User, expected: UserStatus, message: String) {
        if (user.status != expected) {
            throw InvalidUserStatusException(message)
        }
    }
}
