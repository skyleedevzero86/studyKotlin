package com.sleekydz86.oauth.domain.user.port.out

interface PasswordEncoderPort {
    fun encode(rawPassword: String): String

    fun matches(rawPassword: String, encodedPassword: String): Boolean
}
