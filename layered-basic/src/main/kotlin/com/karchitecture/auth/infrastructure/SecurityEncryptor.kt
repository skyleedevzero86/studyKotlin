package com.karchitecture.auth.infrastructure

import com.karchitecture.auth.domain.service.Encryptor
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class SecurityEncryptor(
    private val passwordEncoder: PasswordEncoder
) : Encryptor {

    override fun encrypt(password: String): String {
        return passwordEncoder.encode(password)
    }

    override fun matches(password: String, encodedPassword: String): Boolean {
        return passwordEncoder.matches(password, encodedPassword)
    }
}