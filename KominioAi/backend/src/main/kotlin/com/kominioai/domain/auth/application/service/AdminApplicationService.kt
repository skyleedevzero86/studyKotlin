package com.kominioai.domain.auth.application.service

import com.kominioai.domain.auth.application.dto.*
import com.kominioai.domain.auth.application.port.`in`.AdminUseCase
import com.kominioai.domain.auth.application.port.out.*
import com.kominioai.domain.auth.domain.model.*
import com.kominioai.global.exception.auth.AuthorizationException
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import com.kominioai.domain.auth.domain.repository.UserSecurityLogRepository
import com.kominioai.domain.auth.domain.repository.UserStatisticsRepository


@Service
class AdminApplicationService(
    private val loadUserPort: LoadUserPort,
    private val saveUserPort: SaveUserPort,
    private val loadAuthTokenPort: LoadAuthTokenPort,
    private val saveAuthTokenPort: SaveAuthTokenPort,
    private val userSecurityLogRepository: UserSecurityLogRepository,
    private val userStatisticsRepository: UserStatisticsRepository
) : AdminUseCase {

    override fun getUserList(request: AdminUserListRequest): Mono<AdminUserListResponse> {
        val page = request.page
        val size = request.size
        val offset = (page - 1) * size
        val usersFlux = if (!request.searchQuery.isNullOrBlank()) {
            loadUserPort.searchUsers(request.searchQuery, page, size)
        } else if (!request.accountStatus.isNullOrBlank()) {
            loadUserPort.loadByAccountStatus(request.accountStatus, page, size)
        } else {
            loadUserPort.loadAll(page, size)
        }
        return usersFlux.collectList()
            .zipWith(loadUserPort.countTotalUsers())
            .map { tuple ->
                val users = tuple.t1
                val totalCount = tuple.t2
                AdminUserListResponse(
                    users = users.mapNotNull { user ->
                        user?.let {
                            AdminUserListItem(
                                id = it.id.value,
                                email = it.email.value,
                                username = it.username.value,
                                firstName = it.firstName,
                                lastName = it.lastName,
                                accountStatus = it.accountStatus.name,
                                emailVerified = it.emailVerified,
                                twoFactorEnabled = it.twoFactorEnabled,
                                lastLoginAt = it.lastLoginAt,
                                createdAt = it.createdAt,
                                roles = listOf("USER")
                            )
                        }
                    },
                    totalCount = totalCount,
                    page = page,
                    size = size,
                    totalPages = ((totalCount + size - 1) / size).toInt()
                )
            }
    }

    override fun getUserDetail(userId: String): Mono<AdminUserDetailResponse> {
        return loadUserPort.loadById(UserId(userId))
            .switchIfEmpty(Mono.error(AuthorizationException.AccessDeniedException("user", "ADMIN")))
            .map { user ->
                user?.let {
                    AdminUserDetailResponse(
                        id = it.id.value,
                        email = it.email.value,
                        username = it.username.value,
                        firstName = it.firstName,
                        lastName = it.lastName,
                        profileImageUrl = it.profileImageUrl,
                        phone = it.phone,
                        accountStatus = it.accountStatus.name,
                        emailVerified = it.emailVerified,
                        twoFactorEnabled = it.twoFactorEnabled,
                        lastLoginAt = it.lastLoginAt,
                        failedLoginAttempts = it.failedLoginAttempts,
                        accountLockedUntil = it.accountLockedUntil,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                        roles = listOf(),
                        socialAccounts = listOf(),
                        recentSecurityLogs = listOf()
                    )
                } ?: throw AuthorizationException.AccessDeniedException("user", "ADMIN")
            }
    }

    override fun updateUserStatus(userId: String, request: AdminUserStatusRequest): Mono<Void> {
        return loadUserPort.loadById(UserId(userId))
            .switchIfEmpty(Mono.error(AuthorizationException.AccessDeniedException("user", "ADMIN")))
            .flatMap { user ->
                user?.let {
                    val updatedUser = when (request.status.uppercase()) {
                        "ACTIVE" -> it.activateAccount()
                        "INACTIVE" -> it.deactivateAccount()
                        "SUSPENDED" -> it.suspendAccount()
                        else -> return@flatMap Mono.error<Void>(AuthorizationException.AccessDeniedException("user", "ADMIN"))
                    }
                    saveUserPort.save(updatedUser).then()
                } ?: Mono.error(AuthorizationException.AccessDeniedException("user", "ADMIN"))
            }
    }

    override fun updateUserRole(userId: String, request: AdminUserRoleRequest): Mono<Void> {
        // 실제 UserRoleRepository를 사용하여 역할 부여/회수 구현 필요
        return Mono.empty()
    }

    override fun getSystemStatistics(): Mono<Map<String, Any>> {
        return Mono.zip(
            userStatisticsRepository.countTotalUsers(),
            userStatisticsRepository.countActiveUsers(),
            userStatisticsRepository.countSuspendedUsers(),
            userStatisticsRepository.countAdminUsers(),
            userStatisticsRepository.countUsersRegisteredToday(),
            userStatisticsRepository.countUsersWith2FAEnabled()
        ).map { tuple ->
            mapOf(
                "totalUsers" to tuple.t1,
                "activeUsers" to tuple.t2,
                "suspendedUsers" to tuple.t3,
                "adminUsers" to tuple.t4,
                "usersRegisteredToday" to tuple.t5,
                "usersWith2FAEnabled" to tuple.t6
            )
        }
    }

    override fun getUserSecurityLogs(userId: String, page: Int, size: Int): Mono<AdminSecurityLogResponse> {
        return userSecurityLogRepository.findByUserId(userId, page, size)
            .map { UserSecurityLogMapper.toDto(it) }
            .collectList()
            .zipWith(userSecurityLogRepository.countByUserId(userId))
            .map { tuple ->
                AdminSecurityLogResponse(
                    logs = tuple.t1,
                    totalCount = tuple.t2,
                    page = page,
                    size = size
                )
            }
    }
}