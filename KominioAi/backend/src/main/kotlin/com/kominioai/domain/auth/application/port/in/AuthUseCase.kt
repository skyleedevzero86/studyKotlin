package com.kominioai.domain.auth.application.port.`in`

import com.kominioai.domain.auth.application.dto.LoginRequest
import com.kominioai.domain.auth.application.dto.LoginResponse
import com.kominioai.domain.auth.application.dto.RegisterRequest
import com.kominioai.domain.auth.application.dto.RegisterResponse
import com.kominioai.domain.auth.application.dto.RefreshTokenRequest
import com.kominioai.domain.auth.application.dto.RefreshTokenResponse
import com.kominioai.domain.auth.application.dto.ForgotPasswordRequest
import com.kominioai.domain.auth.application.dto.ResetPasswordRequest
import com.kominioai.domain.auth.application.dto.VerifyEmailRequest
import com.kominioai.domain.auth.application.dto.ResendVerificationRequest
import com.kominioai.domain.auth.application.dto.SocialLoginRequest
import com.kominioai.domain.auth.application.dto.SocialLoginResponse
import reactor.core.publisher.Mono

interface AuthUseCase {
    fun login(request: LoginRequest): Mono<LoginResponse>
    fun register(request: RegisterRequest): Mono<RegisterResponse>
    fun logout(userId: String): Mono<Void>
    fun refreshToken(request: RefreshTokenRequest): Mono<RefreshTokenResponse>
    fun forgotPassword(request: ForgotPasswordRequest): Mono<Void>
    fun resetPassword(request: ResetPasswordRequest): Mono<Void>
    fun verifyEmail(request: VerifyEmailRequest): Mono<Void>
    fun resendVerification(request: ResendVerificationRequest): Mono<Void>
    fun socialLogin(request: SocialLoginRequest): Mono<SocialLoginResponse>
}