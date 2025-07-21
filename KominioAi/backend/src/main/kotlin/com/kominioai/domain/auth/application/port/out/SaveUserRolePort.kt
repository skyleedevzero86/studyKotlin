package com.kominioai.domain.auth.application.port.out

import com.kominioai.domain.auth.domain.model.UserRole
import com.kominioai.domain.auth.domain.model.UserId
import reactor.core.publisher.Mono

interface SaveUserRolePort {
    fun save(role: UserRole): Mono<UserRole>
    fun deleteByUserIdAndRoleName(userId: UserId, roleName: String): Mono<Boolean>
    fun deleteByUserId(userId: UserId): Mono<Boolean>
}