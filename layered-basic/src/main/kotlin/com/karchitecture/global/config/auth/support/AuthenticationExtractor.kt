package com.karchitecture.global.config.auth.support

import jakarta.servlet.http.HttpServletRequest
import java.util.*

object AuthenticationExtractor {

    private const val AUTHORIZATION_HEADER = "Authorization"
    private const val BEARER = "Bearer"
    private const val HEADER_SPLIT_DELIMITER = " "
    private const val TOKEN_TYPE_INDEX = 0
    private const val TOKEN_VALUE_INDEX = 1
    private const val VALID_HEADER_SPLIT_LENGTH = 2

    fun extract(request: HttpServletRequest): Optional<String> {
        val header = request.getHeader(AUTHORIZATION_HEADER)

        if (header.isNullOrBlank()) {
            return Optional.empty()
        }

        return extractFromHeader(header.split(HEADER_SPLIT_DELIMITER).toTypedArray())
    }

    fun extractFromHeader(headerParts: Array<String>): Optional<String> {
        if (headerParts.size == VALID_HEADER_SPLIT_LENGTH &&
            headerParts[TOKEN_TYPE_INDEX] == BEARER
        ) {
            return Optional.ofNullable(headerParts[TOKEN_VALUE_INDEX])
        }
        return Optional.empty()
    }
}