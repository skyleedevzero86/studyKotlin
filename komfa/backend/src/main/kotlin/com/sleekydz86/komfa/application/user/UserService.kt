package com.sleekydz86.komfa.application.user

import com.sleekydz86.komfa.domain.user.ChangePasswordDTO
import com.sleekydz86.komfa.domain.user.FindUsernameDTO
import com.sleekydz86.komfa.domain.user.ForgotPasswordDTO
import com.sleekydz86.komfa.domain.user.JoinRejectedException
import com.sleekydz86.komfa.domain.user.ResetPasswordDTO
import com.sleekydz86.komfa.domain.user.UserEntity
import com.sleekydz86.komfa.domain.user.UserProfileUpdateDTO
import com.sleekydz86.komfa.domain.user.UserRequestDTO
import com.sleekydz86.komfa.domain.user.UserRole
import com.sleekydz86.komfa.domain.user.UserStatus
import com.sleekydz86.komfa.domain.user.WithdrawnAccountException
import com.sleekydz86.komfa.infrastructure.crypto.Aes256Service
import com.sleekydz86.komfa.infrastructure.crypto.sha256Hex
import com.sleekydz86.komfa.infrastructure.persistence.PasswordChangeHistoryRepository
import com.sleekydz86.komfa.infrastructure.persistence.PasswordResetTokenRepository
import com.sleekydz86.komfa.infrastructure.persistence.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val passwordChangeHistoryRepository: PasswordChangeHistoryRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val passwordResetTokenPersistence: PasswordResetTokenPersistence,
    private val authMailPort: AuthMailPort,
    private val aes256Service: Aes256Service,
    @Value("\${komfa.auth.reset-password-base-url:http://localhost:8080}") private val resetPasswordBaseUrl: String,
    @Value("\${komfa.auth.password-remind-days:30}") private val passwordChangeRemindDays: Long,
    @Value("\${komfa.admin.email:}") private val adminEmail: String,
) {

    private val resetTokenValidMinutes = 60L

    fun decryptEmail(user: UserEntity): String? =
        user.email?.let { aes256Service.decryptOrPlain(it) }?.takeIf { it.isNotBlank() }

    @Transactional
    fun join(dto: UserRequestDTO) {
        if (userRepository.findByUsername(dto.username) != null) {
            throw JoinRejectedException("USERNAME_EXISTS", "이미 등록된 아이디입니다. 다른 아이디로 가입해 주세요.")
        }
        val plainEmail = dto.email?.takeIf { it.isNotBlank() }
        if (plainEmail != null && userRepository.findFirstByEmailHash(sha256Hex(plainEmail)) != null) {
            throw JoinRejectedException("EMAIL_EXISTS", "이미 등록된 이메일입니다. 다른 이메일로 가입해 주세요.")
        }
        val now = Instant.now()
        val encryptedEmail = plainEmail?.let { aes256Service.encrypt(it) }
        val emailHash = plainEmail?.let { sha256Hex(it) }
        val entity = UserEntity(
            username = dto.username,
            passwordHash = requireNotNull(passwordEncoder.encode(dto.password)) { "비밀번호 암호화" },
            email = encryptedEmail ?: plainEmail,
            emailHash = emailHash,
            roles = UserRole.USER.authority,
            status = UserStatus.PENDING.name,
            createdAt = now,
            updatedAt = now,
        )
        userRepository.save(entity)
        passwordChangeHistoryRepository.save(
            com.sleekydz86.komfa.domain.user.PasswordChangeHistoryEntity(user = entity, changedAt = now)
        )
        if (adminEmail.isNotBlank()) {
            authMailPort.sendNewSignupNotificationToAdmin(adminEmail, entity.username)
        }
    }

    fun getByUsername(username: String): UserEntity? = userRepository.findByUsername(username)

    fun requirePasswordChange(updatedAt: Instant): Boolean {
        return Instant.now().isAfter(updatedAt.plusSeconds(passwordChangeRemindDays * 24 * 3600))
    }

    @Transactional
    fun updateProfile(username: String, dto: UserProfileUpdateDTO): UserEntity {
        val user = userRepository.findByUsername(username)
            ?: throw NoSuchElementException("사용자를 찾을 수 없습니다: $username")
        if (user.status != UserStatus.ACTIVE.name) throw IllegalStateException("승인된 회원만 정보 수정이 가능합니다.")
        val now = Instant.now()
        val rawInput = dto.email?.takeIf { it.isNotBlank() }
        val plainEmail = when {
            rawInput == null -> decryptEmail(user)
            aes256Service.isEncrypted(rawInput) -> aes256Service.decrypt(rawInput) ?: rawInput
            else -> rawInput
        }
        val encryptedEmail = plainEmail?.let { aes256Service.encrypt(it) } ?: user.email
        val emailHash = plainEmail?.let { sha256Hex(it) }
        val updated = user.copy(email = encryptedEmail, emailHash = emailHash, updatedAt = now)
        return userRepository.save(updated)
    }

    @Transactional
    fun changePassword(username: String, dto: ChangePasswordDTO) {
        val user = userRepository.findByUsername(username)
            ?: throw NoSuchElementException("사용자를 찾을 수 없습니다: $username")
        if (user.status != UserStatus.ACTIVE.name) throw IllegalStateException("승인된 회원만 비밀번호 변경이 가능합니다.")
        if (!passwordEncoder.matches(dto.currentPassword, user.passwordHash)) {
            throw IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.")
        }
        val now = Instant.now()
        val saved = userRepository.save(
            user.copy(passwordHash = requireNotNull(passwordEncoder.encode(dto.newPassword)) { "비밀번호 암호화" }, updatedAt = now)
        )
        passwordChangeHistoryRepository.save(
            com.sleekydz86.komfa.domain.user.PasswordChangeHistoryEntity(user = saved, changedAt = now)
        )
        decryptEmail(saved)?.let { email: String -> authMailPort.sendPasswordChangeNoticeEmail(email, saved.username) }
    }

    fun findUsername(dto: FindUsernameDTO): Boolean {
        val hash = sha256Hex(dto.email.trim())
        val user = userRepository.findFirstByEmailHash(hash) ?: return false
        if (user.status == UserStatus.WITHDRAWN.name) throw WithdrawnAccountException()
        if (user.status != UserStatus.ACTIVE.name) return false
        val plainEmail = decryptEmail(user) ?: return false
        return authMailPort.sendFindUsernameEmail(plainEmail, user.username)
    }

    @Transactional
    fun forgotPassword(dto: ForgotPasswordDTO): Boolean {
        val hash = sha256Hex(dto.email.trim())
        val user = userRepository.findFirstByEmailHash(hash) ?: return true
        if (user.status == UserStatus.WITHDRAWN.name) throw WithdrawnAccountException()
        if (user.status != UserStatus.ACTIVE.name) return true
        val plainEmail = decryptEmail(user) ?: return true
        val tokenValue = UUID.randomUUID().toString()
        val expiresAt = Instant.now().plusSeconds(resetTokenValidMinutes * 60)
        passwordResetTokenPersistence.saveAndFlush(
            com.sleekydz86.komfa.domain.user.PasswordResetTokenEntity(
                tokenValue = tokenValue,
                user = user,
                expiresAt = expiresAt,
            ),
        )
        val resetLink = "$resetPasswordBaseUrl/reset-password?token=$tokenValue"
        return try {
            authMailPort.sendResetPasswordEmail(requireNotNull(plainEmail) { "이메일" }, resetLink)
        } catch (_: Exception) {
            true
        }
    }

    @Transactional
    fun resetPassword(dto: ResetPasswordDTO): Boolean {
        val tokenEntity = passwordResetTokenRepository.findByTokenValue(dto.token) ?: return false
        if (tokenEntity.expiresAt.isBefore(Instant.now())) {
            passwordResetTokenRepository.delete(tokenEntity)
            return false
        }
        val user = tokenEntity.user
        val now = Instant.now()
        userRepository.save(
            user.copy(passwordHash = requireNotNull(passwordEncoder.encode(dto.newPassword)) { "비밀번호 암호화" }, updatedAt = now)
        )
        passwordChangeHistoryRepository.save(
            com.sleekydz86.komfa.domain.user.PasswordChangeHistoryEntity(user = user, changedAt = now)
        )
        passwordResetTokenRepository.delete(tokenEntity)
        decryptEmail(user)?.let { email -> authMailPort.sendPasswordChangeNoticeEmail(requireNotNull(email) { "이메일" }, user.username) }
        return true
    }

    @Transactional
    fun approve(userId: Long): UserEntity {
        val user = userRepository.findById(userId).orElseThrow { NoSuchElementException("사용자를 찾을 수 없습니다: $userId") }
        if (user.status == UserStatus.WITHDRAWN.name) throw IllegalStateException("탈퇴한 계정은 수정하거나 삭제할 수 없습니다.")
        return userRepository.save(user.copy(status = UserStatus.ACTIVE.name, updatedAt = Instant.now()))
    }

    @Transactional
    fun suspend(userId: Long): UserEntity {
        val user = userRepository.findById(userId).orElseThrow { NoSuchElementException("사용자를 찾을 수 없습니다: $userId") }
        if (user.status == UserStatus.WITHDRAWN.name) throw IllegalStateException("탈퇴한 계정은 수정하거나 삭제할 수 없습니다.")
        return userRepository.save(user.copy(status = UserStatus.SUSPENDED.name, updatedAt = Instant.now()))
    }

    @Transactional
    fun withdrawByAdmin(userId: Long): UserEntity {
        val user = userRepository.findById(userId).orElseThrow { NoSuchElementException("사용자를 찾을 수 없습니다: $userId") }
        if (user.status == UserStatus.WITHDRAWN.name) throw IllegalStateException("탈퇴한 계정은 수정하거나 삭제할 수 없습니다.")
        return userRepository.save(user.copy(status = UserStatus.WITHDRAWN.name, updatedAt = Instant.now()))
    }

    @Transactional
    fun updateRoles(userId: Long, newRoles: String): UserEntity {
        val user = userRepository.findById(userId).orElseThrow { NoSuchElementException("사용자를 찾을 수 없습니다: $userId") }
        if (user.status == UserStatus.WITHDRAWN.name) throw IllegalStateException("탈퇴한 계정은 수정할 수 없습니다.")
        val normalized = newRoles.trim().let { if (it.startsWith("ROLE_")) it else "ROLE_$it" }
        if (normalized != UserRole.USER.authority && normalized != UserRole.ADMIN.authority) {
            throw IllegalArgumentException("역할은 ROLE_USER 또는 ROLE_ADMIN 만 가능합니다.")
        }
        if (user.roles.contains(UserRole.ADMIN.authority) && normalized == UserRole.USER.authority) {
            val adminCount = userRepository.findAll().count { it.roles.contains(UserRole.ADMIN.authority) }
            if (adminCount <= 1) throw IllegalStateException("관리자는 최소 1명 이상이어야 합니다.")
        }
        return userRepository.save(user.copy(roles = normalized, updatedAt = Instant.now()))
    }

    @Transactional
    fun withdrawSelf(username: String): UserEntity {
        val user = userRepository.findByUsername(username)
            ?: throw NoSuchElementException("사용자를 찾을 수 없습니다: $username")
        if (user.status == UserStatus.WITHDRAWN.name) throw IllegalStateException("이미 탈퇴 처리된 계정입니다.")
        return userRepository.save(user.copy(status = UserStatus.WITHDRAWN.name, updatedAt = Instant.now()))
    }
}
