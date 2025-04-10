package com.karchitecture.auth.domain.service

import com.karchitecture.auth.domain.Auth

interface AuthRepository {

    fun save(auth: Auth): Auth

    fun findByUsername(username: String): Auth?

    fun existsByUsername(username: String): Boolean
}
