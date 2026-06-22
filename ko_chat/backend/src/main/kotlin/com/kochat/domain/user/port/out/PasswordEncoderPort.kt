package com.kochat.domain.user.port.out

interface PasswordEncoderPort {
    fun encode(rawPassword: String): String

    fun matches(rawPassword: String, encodedPassword: String): Boolean
}
