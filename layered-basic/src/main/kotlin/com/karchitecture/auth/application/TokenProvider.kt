package com.karchitecture.auth.application

interface TokenProvider {

    fun create(id: Long): String

    fun extract(token: String): Long
}