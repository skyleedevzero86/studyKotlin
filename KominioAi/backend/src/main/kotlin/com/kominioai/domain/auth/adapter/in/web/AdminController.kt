package com.kominioai.domain.auth.adapter.`in`.web

import com.kominioai.domain.auth.application.dto.*
import com.kominioai.domain.auth.application.port.`in`.AdminUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import jakarta.validation.Valid
import com.kominioai.domain.auth.domain.repository.UserSecurityLogRepository
import com.kominioai.domain.auth.domain.repository.UserStatisticsRepository

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val adminUseCase: AdminUseCase
) {
    @GetMapping("/users")
    fun getUserList(@Valid @ModelAttribute request: AdminUserListRequest): Mono<ResponseEntity<AdminUserListResponse>> =
        adminUseCase.getUserList(request)
            .map { ResponseEntity.ok(it) }

    @GetMapping("/users/{userId}")
    fun getUserDetail(@PathVariable userId: String): Mono<ResponseEntity<AdminUserDetailResponse>> =
        adminUseCase.getUserDetail(userId)
            .map { ResponseEntity.ok(it) }

    @PutMapping("/users/{userId}/status")
    fun updateUserStatus(@PathVariable userId: String, @Valid @RequestBody request: AdminUserStatusRequest): Mono<ResponseEntity<Void>> =
        adminUseCase.updateUserStatus(userId, request)
            .thenReturn(ResponseEntity.ok().build())

    @PutMapping("/users/{userId}/role")
    fun updateUserRole(@PathVariable userId: String, @Valid @RequestBody request: AdminUserRoleRequest): Mono<ResponseEntity<Void>> =
        adminUseCase.updateUserRole(userId, request)
            .thenReturn(ResponseEntity.ok().build())

    @GetMapping("/users/{userId}/security-logs")
    fun getUserSecurityLogs(@PathVariable userId: String, @RequestParam page: Int, @RequestParam size: Int): Mono<ResponseEntity<AdminSecurityLogResponse>> =
        adminUseCase.getUserSecurityLogs(userId, page, size)
            .map { ResponseEntity.ok(it) }

    @GetMapping("/statistics")
    fun getSystemStatistics(): Mono<ResponseEntity<Map<String, Any>>> =
        adminUseCase.getSystemStatistics()
            .map { ResponseEntity.ok(it) }
}