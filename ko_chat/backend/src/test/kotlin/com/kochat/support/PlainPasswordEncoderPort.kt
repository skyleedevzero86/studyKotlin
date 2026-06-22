package com.kochat.support

import com.kochat.domain.user.port.out.PasswordEncoderPort

class PlainPasswordEncoderPort : PasswordEncoderPort {

    override fun encode(rawPassword: String): String = "encoded:$rawPassword"

    override fun matches(rawPassword: String, encodedPassword: String): Boolean =
        encode(rawPassword) == encodedPassword
}
