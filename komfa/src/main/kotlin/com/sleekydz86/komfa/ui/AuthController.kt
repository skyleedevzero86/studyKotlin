package com.sleekydz86.komfa.ui

import com.sleekydz86.komfa.application.user.UserService
import com.sleekydz86.komfa.domain.user.FindUsernameDTO
import com.sleekydz86.komfa.domain.user.ForgotPasswordDTO
import com.sleekydz86.komfa.domain.user.ResetPasswordDTO
import com.sleekydz86.komfa.domain.user.WithdrawnAccountException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService,
) {

    @PostMapping("/find-username")
    fun findUsername(@RequestBody dto: FindUsernameDTO): ResponseEntity<Map<String, String>> {
        return try {
            val sent = userService.findUsername(dto)
            ResponseEntity.ok(
                mapOf("message" to "등록된 이메일이 있으면 아이디 안내 메일을 발송했습니다.")
            )
        } catch (e: WithdrawnAccountException) {
            ResponseEntity.badRequest().body(mapOf("code" to "WITHDRAWN_ACCOUNT", "message" to (e.message ?: "탈퇴한 계정입니다. 관리자에게 문의하세요.")))
        }
    }

    @PostMapping("/forgot-password")
    fun forgotPassword(@RequestBody dto: ForgotPasswordDTO): ResponseEntity<Map<String, String>> {
        return try {
            userService.forgotPassword(dto)
            ResponseEntity.ok(
                mapOf("message" to "등록된 이메일이 있으면 비밀번호 재설정 링크를 발송했습니다.")
            )
        } catch (e: WithdrawnAccountException) {
            ResponseEntity.badRequest().body(mapOf("code" to "WITHDRAWN_ACCOUNT", "message" to (e.message ?: "탈퇴한 계정입니다. 관리자에게 문의하세요.")))
        }
    }

    @PostMapping("/reset-password")
    fun resetPassword(@RequestBody dto: ResetPasswordDTO): ResponseEntity<Map<String, String>> {
        val ok = userService.resetPassword(dto)
        return if (ok) ResponseEntity.ok(mapOf("message" to "비밀번호가 변경되었습니다."))
        else ResponseEntity.badRequest().body(mapOf("message" to "링크가 만료되었거나 유효하지 않습니다."))
    }
}
