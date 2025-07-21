package com.kominioai.domain.auth.application.service

import com.kominioai.domain.auth.application.dto.*
import com.kominioai.domain.auth.application.port.`in`.TwoFactorAuthUseCase
import com.kominioai.domain.auth.application.port.out.*
import com.kominioai.domain.auth.domain.model.*
import com.kominioai.domain.auth.domain.service.TwoFactorAuthService
import com.kominioai.global.exception.auth.AuthenticationException
import com.kominioai.global.exception.domain.DomainException
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class TwoFactorAuthApplicationService(
    private val loadUserPort: LoadUserPort,
    private val saveUserPort: SaveUserPort,
    private val cachePort: CachePort,
    private val emailPort: EmailPort,
    private val twoFactorAuthService: TwoFactorAuthService
) : TwoFactorAuthUseCase {

    override fun enableTwoFactor(userId: String, request: EnableTwoFactorRequest): Mono<EnableTwoFactorResponse> {
        return loadUserPort.loadById(UserId(userId))
            .switchIfEmpty(Mono.error(AuthenticationException.InvalidCredentialsException()))
            .flatMap { user ->
                user?.let {
                    val secret = twoFactorAuthService.generateOTP()
                    val backupCodes = twoFactorAuthService.generateBackupCodes()

                    val updatedUser = it.enableTwoFactor(secret)
                    saveUserPort.save(updatedUser)
                        .flatMap { savedUser ->
                            savedUser?.let { saved ->
                                cachePort.set("2fa:${it.id.value}", request.otpCode, 300)
                                    .thenReturn(
                                        EnableTwoFactorResponse(
                                            secret = secret,
                                            qrCodeUrl = twoFactorAuthService.generateQRCodeUrl(
                                                saved.username.value,
                                                secret,
                                                "KominioAI"
                                            ),
                                            backupCodes = backupCodes
                                        )
                                    )
                            } ?: Mono.error(AuthenticationException.InvalidCredentialsException())
                        }
                } ?: Mono.error(AuthenticationException.InvalidCredentialsException())
            }
    }

    override fun verifyTwoFactor(userId: String, request: VerifyTwoFactorRequest): Mono<Void> {
        return loadUserPort.loadById(UserId(userId))
            .switchIfEmpty(Mono.error(AuthenticationException.InvalidCredentialsException()))
            .flatMap { user ->
                user?.let {
                    if (!it.twoFactorEnabled) {
                        return@flatMap Mono.error<Void>(DomainException.ValidationException("2FA is not enabled"))
                    }
                    cachePort.get("2fa:${it.id.value}")
                        .switchIfEmpty(Mono.error(AuthenticationException.InvalidCredentialsException()))
                        .flatMap { cachedOtp ->
                            if (cachedOtp != null && twoFactorAuthService.validateOTP(request.otpCode, cachedOtp)) {
                                cachePort.delete("2fa:${it.id.value}").then()
                            } else {
                                Mono.error(AuthenticationException.InvalidCredentialsException())
                            }
                        }
                } ?: Mono.error(AuthenticationException.InvalidCredentialsException())
            }
    }

    override fun disableTwoFactor(userId: String, request: DisableTwoFactorRequest): Mono<Void> {
        return loadUserPort.loadById(UserId(userId))
            .switchIfEmpty(Mono.error(AuthenticationException.InvalidCredentialsException()))
            .flatMap { user ->
                user?.let {
                    if (!it.twoFactorEnabled) {
                        return@flatMap Mono.error<Void>(DomainException.ValidationException("2FA is not enabled"))
                    }
                    cachePort.get("2fa:${it.id.value}")
                        .switchIfEmpty(Mono.error(AuthenticationException.InvalidCredentialsException()))
                        .flatMap { cachedOtp ->
                            if (cachedOtp != null && twoFactorAuthService.validateOTP(request.otpCode, cachedOtp)) {
                                val updatedUser = it.disableTwoFactor()
                                saveUserPort.save(updatedUser)
                                    .then(cachePort.delete("2fa:${it.id.value}"))
                                    .then()
                            } else {
                                Mono.error(AuthenticationException.InvalidCredentialsException())
                            }
                        }
                } ?: Mono.error(AuthenticationException.InvalidCredentialsException())
            }
    }

    override fun generateBackupCodes(userId: String): Mono<GenerateBackupCodesResponse> {
        return loadUserPort.loadById(UserId(userId))
            .switchIfEmpty(Mono.error(AuthenticationException.InvalidCredentialsException()))
            .flatMap { user ->
                user?.let {
                    if (!it.twoFactorEnabled) {
                        return@flatMap Mono.error<GenerateBackupCodesResponse>(DomainException.ValidationException("2FA is not enabled"))
                    }
                    val backupCodes = twoFactorAuthService.generateBackupCodes()
                    Mono.just(GenerateBackupCodesResponse(backupCodes = backupCodes))
                } ?: Mono.error(AuthenticationException.InvalidCredentialsException())
            }
    }

    override fun verifyBackupCode(userId: String, request: VerifyBackupCodeRequest): Mono<Void> {
        return loadUserPort.loadById(UserId(userId))
            .switchIfEmpty(Mono.error(AuthenticationException.InvalidCredentialsException()))
            .flatMap { user ->
                user?.let {
                    if (!it.twoFactorEnabled) {
                        return@flatMap Mono.error<Void>(DomainException.ValidationException("2FA is not enabled"))
                    }
                    cachePort.get("backup_codes:${it.id.value}")
                        .switchIfEmpty(Mono.error(AuthenticationException.InvalidCredentialsException()))
                        .flatMap { cachedCodes ->
                            if (cachedCodes != null) {
                                val backupCodes = cachedCodes.split(",")
                                if (twoFactorAuthService.validateBackupCode(request.backupCode, backupCodes)) {
                                    val updatedCodes = backupCodes.filter { code -> code != request.backupCode.uppercase() }
                                    cachePort.set("backup_codes:${it.id.value}", updatedCodes.joinToString(","), 3600)
                                        .then()
                                } else {
                                    Mono.error(AuthenticationException.InvalidCredentialsException())
                                }
                            } else {
                                Mono.error(AuthenticationException.InvalidCredentialsException())
                            }
                        }
                } ?: Mono.error(AuthenticationException.InvalidCredentialsException())
            }
    }
}