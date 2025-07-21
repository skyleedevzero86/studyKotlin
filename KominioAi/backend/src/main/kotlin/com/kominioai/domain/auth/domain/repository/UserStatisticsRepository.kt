package com.kominioai.domain.auth.domain.repository

import reactor.core.publisher.Mono

interface UserStatisticsRepository {
    fun countTotalUsers(): Mono<Long>
    fun countActiveUsers(): Mono<Long>
    fun countSuspendedUsers(): Mono<Long>
    fun countAdminUsers(): Mono<Long>
    fun countUsersRegisteredToday(): Mono<Long>
    fun countUsersWith2FAEnabled(): Mono<Long>
}