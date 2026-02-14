package com.sleekydz86.komfa.application.user

import com.sleekydz86.komfa.domain.user.PasswordResetTokenEntity
import com.sleekydz86.komfa.infrastructure.persistence.PasswordResetTokenRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class PasswordResetTokenPersistence(
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun saveAndFlush(entity: PasswordResetTokenEntity) {
        passwordResetTokenRepository.save(entity)
        passwordResetTokenRepository.flush()
    }
}
