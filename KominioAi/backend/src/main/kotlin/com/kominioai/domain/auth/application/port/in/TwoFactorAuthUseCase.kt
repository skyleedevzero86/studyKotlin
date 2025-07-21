package com.kominioai.domain.auth.application.port.`in`

import com.kominioai.domain.auth.application.dto.EnableTwoFactorRequest
import com.kominioai.domain.auth.application.dto.EnableTwoFactorResponse
import com.kominioai.domain.auth.application.dto.VerifyTwoFactorRequest
import com.kominioai.domain.auth.application.dto.DisableTwoFactorRequest
import com.kominioai.domain.auth.application.dto.GenerateBackupCodesResponse
import com.kominioai.domain.auth.application.dto.VerifyBackupCodeRequest
import reactor.core.publisher.Mono

interface TwoFactorAuthUseCase {
    fun enableTwoFactor(userId: String, request: EnableTwoFactorRequest): Mono<EnableTwoFactorResponse>
    fun verifyTwoFactor(userId: String, request: VerifyTwoFactorRequest): Mono<Void>
    fun disableTwoFactor(userId: String, request: DisableTwoFactorRequest): Mono<Void>
    fun generateBackupCodes(userId: String): Mono<GenerateBackupCodesResponse>
    fun verifyBackupCode(userId: String, request: VerifyBackupCodeRequest): Mono<Void>
}