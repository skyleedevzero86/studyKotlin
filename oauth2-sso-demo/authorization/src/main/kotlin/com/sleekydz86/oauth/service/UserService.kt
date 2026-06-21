package com.sleekydz86.oauth.service

import com.sleekydz86.oauth.domain.user.User
import com.sleekydz86.oauth.domain.user.UserRepository
import com.sleekydz86.oauth.dto.SignupRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    @Transactional
    fun signup(request: SignupRequest): User {
        if (userRepository.existsByUsername(request.username)) {
            throw IllegalArgumentException("Username already exists: ${request.username}")
        }

        val encodedPassword = requireNotNull(passwordEncoder.encode(request.password))
        return userRepository.save(
            User(
                username = request.username,
                password = encodedPassword,
                role = request.role,
            ),
        )
    }
}
