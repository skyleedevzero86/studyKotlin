package com.kominioai.domain.auth.application.port.`in`

import com.kominioai.domain.auth.application.dto.AdminUserListRequest
import com.kominioai.domain.auth.application.dto.AdminUserListResponse
import com.kominioai.domain.auth.application.dto.AdminUserDetailResponse
import com.kominioai.domain.auth.application.dto.AdminUserStatusRequest
import com.kominioai.domain.auth.application.dto.AdminUserRoleRequest
import com.kominioai.domain.auth.application.dto.AdminSecurityLogResponse
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

interface AdminUseCase {
    fun getUserList(request: AdminUserListRequest): Mono<AdminUserListResponse>
    fun getUserDetail(userId: String): Mono<AdminUserDetailResponse>
    fun updateUserStatus(userId: String, request: AdminUserStatusRequest): Mono<Void>
    fun updateUserRole(userId: String, request: AdminUserRoleRequest): Mono<Void>
    fun getUserSecurityLogs(userId: String, page: Int, size: Int): Mono<AdminSecurityLogResponse>
    fun getSystemStatistics(): Mono<Map<String, Any>>
}