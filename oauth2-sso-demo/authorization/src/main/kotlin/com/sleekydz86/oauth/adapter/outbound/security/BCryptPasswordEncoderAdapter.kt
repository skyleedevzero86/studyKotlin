package com.sleekydz86.oauth.adapter.outbound.security

import com.sleekydz86.oauth.domain.user.port.out.PasswordEncoderPort
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class BCryptPasswordEncoderAdapter(
    private val passwordEncoder: PasswordEncoder,
) : PasswordEncoderPort {

    override fun encode(rawPassword: String): String =
        requireNotNull(passwordEncoder.encode(rawPassword))

    override fun matches(rawPassword: String, encodedPassword: String): Boolean =
        passwordEncoder.matches(rawPassword, encodedPassword)
}
