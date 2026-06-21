package com.sleekydz86.oauth.support

import com.sleekydz86.oauth.domain.user.port.out.PasswordEncoderPort

class PlainPasswordEncoderPort : PasswordEncoderPort {

    override fun encode(rawPassword: String): String = "encoded:$rawPassword"

    override fun matches(rawPassword: String, encodedPassword: String): Boolean =
        encode(rawPassword) == encodedPassword
}
