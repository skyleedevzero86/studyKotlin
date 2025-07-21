package com.kominioai.domain.auth.application.port.out

import reactor.core.publisher.Mono

interface CachePort {
    fun set(key: String, value: String, ttlSeconds: Long): Mono<Boolean>
    fun get(key: String): Mono<String?>
    fun delete(key: String): Mono<Boolean>
    fun exists(key: String): Mono<Boolean>
    fun increment(key: String): Mono<Long>
    fun increment(key: String, delta: Long): Mono<Long>
    fun expire(key: String, ttlSeconds: Long): Mono<Boolean>
}