package com.sleekydz86.komfa.ui

import com.sleekydz86.komfa.application.user.UserService
import com.sleekydz86.komfa.infrastructure.crypto.Aes256Service
import com.sleekydz86.komfa.infrastructure.persistence.PasswordChangeHistoryRepository
import com.sleekydz86.komfa.infrastructure.persistence.UserRepository
import com.sleekydz86.komfa.ui.dto.AdminPasswordHistoryResponse
import com.sleekydz86.komfa.ui.dto.AdminUserListItem
import com.sleekydz86.komfa.ui.dto.AdminUserListResponse
import com.sleekydz86.komfa.ui.dto.PasswordHistoryItem
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

private fun maskEmail(encryptedOrNull: String?): String? {
    if (encryptedOrNull.isNullOrBlank()) return null
    return "***@***"
}

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
class AdminUsersController(
    private val userRepository: UserRepository,
    private val passwordChangeHistoryRepository: PasswordChangeHistoryRepository,
    private val aes256Service: Aes256Service,
    private val userService: UserService,
) {

    @GetMapping
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?,
    ): ResponseEntity<AdminUserListResponse> {
        val pageable = PageRequest.of(page, size)
        val q = search?.trim()?.takeIf { it.isNotBlank() }
        val result = userRepository.findAllBySearch(q, pageable)
        val content = result.content.map { u ->
            AdminUserListItem(
                id = u.id!!,
                username = u.username,
                emailMasked = maskEmail(u.email),
                roles = u.roles,
                status = u.status,
                createdAt = u.createdAt.toString(),
                updatedAt = u.updatedAt.toString(),
            )
        }
        return ResponseEntity.ok(
            AdminUserListResponse(
                content = content,
                totalElements = result.totalElements,
                totalPages = result.totalPages,
                number = result.number,
                size = result.size,
            )
        )
    }

    @GetMapping("/{userId}/password-history")
    fun passwordHistory(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<AdminPasswordHistoryResponse> {
        val user = userRepository.findById(userId).orElse(null) ?: return ResponseEntity.notFound().build()
        val pageable = PageRequest.of(page, size)
        val result = passwordChangeHistoryRepository.findByUserIdOrderByChangedAtDesc(userId, pageable)
        val content = result.content.map { h ->
            PasswordHistoryItem(id = h.id!!, changedAt = h.changedAt.toString())
        }
        return ResponseEntity.ok(
            AdminPasswordHistoryResponse(
                userId = user.id!!,
                username = user.username,
                content = content,
                totalElements = result.totalElements,
                totalPages = result.totalPages,
                number = result.number,
                size = result.size,
            )
        )
    }

    @GetMapping("/{userId}/sensitive/email")
    fun getDecryptedEmail(@PathVariable userId: Long): ResponseEntity<Map<String, String?>> {
        val user = userRepository.findById(userId).orElse(null) ?: return ResponseEntity.notFound().build()
        val decrypted = user.email?.let { aes256Service.decryptOrPlain(it) }
        return ResponseEntity.ok(mapOf("email" to decrypted))
    }

    @PostMapping("/{userId}/approve")
    fun approve(@PathVariable userId: Long): ResponseEntity<Any> {
        return try {
            val user = userService.approve(userId)
            ResponseEntity.ok(toListItem(user))
        } catch (_: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(mapOf("message" to (e.message ?: "탈퇴한 계정은 수정하거나 삭제할 수 없습니다.")))
        }
    }

    @PostMapping("/{userId}/suspend")
    fun suspend(@PathVariable userId: Long): ResponseEntity<Any> {
        return try {
            val user = userService.suspend(userId)
            ResponseEntity.ok(toListItem(user))
        } catch (_: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(mapOf("message" to (e.message ?: "탈퇴한 계정은 수정하거나 삭제할 수 없습니다.")))
        }
    }

    @PostMapping("/{userId}/withdraw")
    fun withdraw(@PathVariable userId: Long): ResponseEntity<Any> {
        return try {
            val user = userService.withdrawByAdmin(userId)
            ResponseEntity.ok(toListItem(user))
        } catch (_: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(mapOf("message" to (e.message ?: "탈퇴한 계정은 수정하거나 삭제할 수 없습니다.")))
        }
    }

    private fun toListItem(u: com.sleekydz86.komfa.domain.user.UserEntity) = AdminUserListItem(
        id = u.id!!,
        username = u.username,
        emailMasked = maskEmail(u.email),
        roles = u.roles,
        status = u.status,
        createdAt = u.createdAt.toString(),
        updatedAt = u.updatedAt.toString(),
    )
}
