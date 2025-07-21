package com.kominioai.domain.auth.application.port.out

import com.kominioai.domain.auth.domain.model.UserId
import com.kominioai.domain.auth.domain.model.UserRole
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface LoadUserRolePort {
    fun loadByUserId(userId: UserId): Flux<UserRole>
    fun loadByUserIdAndRoleName(userId: UserId, roleName: String): Mono<UserRole?>
    fun loadActiveRolesByUserId(userId: UserId): Flux<UserRole>
}