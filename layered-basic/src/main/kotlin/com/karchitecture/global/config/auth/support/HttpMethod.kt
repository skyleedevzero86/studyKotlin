package com.karchitecture.global.config.auth.support

enum class HttpMethod {
    GET,
    POST,
    PUT,
    PATCH,
    DELETE,
    OPTIONS,
    HEAD,
    TRACE,
    CONNECT,
    ANY;

    fun matches(pathMethod: String): Boolean {
        return this == ANY ||
                this.name.equals(pathMethod, ignoreCase = true)
    }
}
