package com.komroonga.global.config

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

class CustomPasswordEncoder : PasswordEncoder {
    private val delegate = BCryptPasswordEncoder()

    override fun encode(rawPassword: CharSequence?): String {
        return delegate.encode(rawPassword)
    }

    override fun matches(rawPassword: CharSequence?, encodedPassword: String?): Boolean {
        val adjusted = encodedPassword?.removePrefix("{bcrypt}") ?: ""
        return delegate.matches(rawPassword, adjusted)
    }
}
