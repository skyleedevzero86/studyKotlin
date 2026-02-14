package com.sleekydz86.komfa.ui

import com.sleekydz86.komfa.application.user.UserService
import com.sleekydz86.komfa.domain.user.ChangePasswordDTO
import com.sleekydz86.komfa.domain.user.UserProfileUpdateDTO
import com.sleekydz86.komfa.ui.dto.MeResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class MeController(
    private val userService: UserService,
) {

    @GetMapping("/me")
    fun me(@AuthenticationPrincipal principal: UserDetails?): ResponseEntity<MeResponse> {
        val username = principal?.username ?: return ResponseEntity.notFound().build()
        val user = userService.getByUsername(username) ?: return ResponseEntity.notFound().build()
        val requirePasswordChange = userService.requirePasswordChange(user.updatedAt)
        val decryptedEmail = userService.decryptEmail(user)
        return ResponseEntity.ok(
            MeResponse(
                username = user.username,
                email = decryptedEmail,
                createdAt = user.createdAt.toString(),
                updatedAt = user.updatedAt.toString(),
                requirePasswordChange = requirePasswordChange,
            )
        )
    }

    @PatchMapping("/me")
    fun updateProfile(
        @AuthenticationPrincipal principal: UserDetails?,
        @RequestBody dto: UserProfileUpdateDTO,
    ): ResponseEntity<Unit> {
        val username = principal?.username ?: return ResponseEntity.notFound().build()
        return try {
            userService.updateProfile(username, dto)
            ResponseEntity.ok().build()
        } catch (_: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/auth/change-password")
    fun changePassword(
        @AuthenticationPrincipal principal: UserDetails?,
        @RequestBody dto: ChangePasswordDTO,
    ): ResponseEntity<Unit> {
        val username = principal?.username ?: return ResponseEntity.status(401).build()
        return try {
            userService.changePassword(username, dto)
            ResponseEntity.ok().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    @PostMapping("/me/withdraw")
    fun withdraw(@AuthenticationPrincipal principal: UserDetails?): ResponseEntity<Map<String, String>> {
        val username = principal?.username ?: return ResponseEntity.status(401).build()
        return try {
            userService.withdrawSelf(username)
            ResponseEntity.ok(mapOf("message" to "탈퇴되었습니다."))
        } catch (_: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(mapOf("message" to (e.message ?: "이미 탈퇴 처리된 계정입니다.")))
        }
    }
}
