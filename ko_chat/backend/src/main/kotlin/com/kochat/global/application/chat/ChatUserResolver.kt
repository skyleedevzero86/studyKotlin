package com.kochat.global.application.chat

import com.kochat.domain.user.exception.UserNotFoundException
import com.kochat.domain.user.port.out.UserPersistencePort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatUserResolver(
    private val userPersistencePort: UserPersistencePort,
) {
    @Transactional(readOnly = true)
    fun resolveUserId(username: String): Long {
        val user = userPersistencePort.findByUsername(username)
            ?: throw UserNotFoundException(username)
        return user.id ?: throw UserNotFoundException(username)
    }
}
