package com.sleekydz86.komfa.infrastructure.persistence

import com.sleekydz86.komfa.domain.user.PasswordChangeHistoryEntity
import com.sleekydz86.komfa.domain.user.UserEntity
import com.sleekydz86.komfa.domain.user.UserStatus
import com.sleekydz86.komfa.infrastructure.crypto.Aes256Service
import com.sleekydz86.komfa.infrastructure.crypto.sha256Hex
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Instant

@Configuration
class DataInitializer(
    private val userRepository: UserRepository,
    private val passwordChangeHistoryRepository: PasswordChangeHistoryRepository,
    private val passwordEncoder: PasswordEncoder,
    private val aes256Service: Aes256Service,
) {

    @Bean
    fun initUser(): ApplicationRunner = ApplicationRunner {
        if (userRepository.findByUsername("user") == null) {
            val plainEmail = "user@example.com"
            val now = Instant.now()
            val email: String = aes256Service.encrypt(plainEmail)
            val emailHash: String = sha256Hex(plainEmail)
            val user = userRepository.save(
                UserEntity(
                    username = "user",
                    passwordHash = requireNotNull(passwordEncoder.encode("password")) { "비밀번호 암호화" },
                    email = email,
                    emailHash = emailHash,
                    roles = "ROLE_USER",
                    status = UserStatus.PENDING.name,
                    createdAt = now,
                    updatedAt = now,
                )
            )
            passwordChangeHistoryRepository.save(PasswordChangeHistoryEntity(user = user, changedAt = now))
        }
    }
}
