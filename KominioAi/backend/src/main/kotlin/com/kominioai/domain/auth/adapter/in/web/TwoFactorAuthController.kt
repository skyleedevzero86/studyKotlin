package com.kominioai.domain.auth.adapter.`in`.web

import com.kominioai.domain.auth.application.dto.*
import com.kominioai.domain.auth.application.port.`in`.TwoFactorAuthUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/auth/2fa")
class TwoFactorAuthController(
    private val twoFactorAuthUseCase: TwoFactorAuthUseCase
) {
    @PostMapping("/enable")
    fun enableTwoFactor(@RequestHeader("X-USER-ID") userId: String, @Valid @RequestBody request: EnableTwoFactorRequest): Mono<ResponseEntity<EnableTwoFactorResponse>> =
        twoFactorAuthUseCase.enableTwoFactor(userId, request)
            .map { ResponseEntity.ok(it) }

    @PostMapping("/verify")
    fun verifyTwoFactor(@RequestHeader("X-USER-ID") userId: String, @Valid @RequestBody request: VerifyTwoFactorRequest): Mono<ResponseEntity<Void>> =
        twoFactorAuthUseCase.verifyTwoFactor(userId, request)
            .thenReturn(ResponseEntity.ok().build())

    @PostMapping("/disable")
    fun disableTwoFactor(@RequestHeader("X-USER-ID") userId: String, @Valid @RequestBody request: DisableTwoFactorRequest): Mono<ResponseEntity<Void>> =
        twoFactorAuthUseCase.disableTwoFactor(userId, request)
            .thenReturn(ResponseEntity.ok().build())

    @PostMapping("/backup-codes")
    fun generateBackupCodes(@RequestHeader("X-USER-ID") userId: String): Mono<ResponseEntity<GenerateBackupCodesResponse>> =
        twoFactorAuthUseCase.generateBackupCodes(userId)
            .map { ResponseEntity.ok(it) }

    @PostMapping("/verify-backup")
    fun verifyBackupCode(@RequestHeader("X-USER-ID") userId: String, @Valid @RequestBody request: VerifyBackupCodeRequest): Mono<ResponseEntity<Void>> =
        twoFactorAuthUseCase.verifyBackupCode(userId, request)
            .thenReturn(ResponseEntity.ok().build())
}