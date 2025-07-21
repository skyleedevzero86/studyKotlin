package com.kominioai.domain.auth.application.port.out

import reactor.core.publisher.Mono

interface EmailPort {
    fun sendVerificationEmail(to: String, token: String, username: String): Mono<Void>
    fun sendPasswordResetEmail(to: String, token: String, username: String): Mono<Void>
    fun sendWelcomeEmail(to: String, username: String): Mono<Void>
    fun sendSecurityAlertEmail(to: String, username: String, activity: String): Mono<Void>
    fun sendTwoFactorSetupEmail(to: String, username: String, secret: String): Mono<Void>
}