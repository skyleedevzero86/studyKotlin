package com.kominioai.domain.auth.adapter.out.event

import com.kominioai.domain.auth.domain.event.*
import com.kominioai.domain.auth.application.port.out.EmailPort
import com.kominioai.domain.auth.application.port.out.CachePort
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class AuthEventHandler(
    private val emailPort: EmailPort,
    private val cachePort: CachePort
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener
    @Async
    fun handleUserRegistered(event: UserRegisteredEvent) {
        log.info("User registered: ${event.userId}, ${event.email}")

        emailPort.sendWelcomeEmail(event.email, event.username)
            .subscribe(
                { log.info("Welcome email sent to ${event.email}") },
                { error -> log.error("Failed to send welcome email to ${event.email}", error) }
            )
    }

    @EventListener
    @Async
    fun handleUserLoggedIn(event: UserLoggedInEvent) {
        log.info("User logged in: ${event.userId}, ${event.email}")

        val loginKey = "login:${event.userId}:${LocalDateTime.now().toLocalDate()}"
        cachePort.increment(loginKey, 1)
            .subscribe(
                { count -> log.info("Login count for ${event.userId}: $count") },
                { error -> log.error("Failed to increment login count", error) }
            )
    }

    @EventListener
    @Async
    fun handleAuthFailed(event: AuthFailedEvent) {
        log.warn("Auth failed: ${event.email}, reason: ${event.failureReason}")

        val failedKey = "auth_failed:${event.email}:${LocalDateTime.now().toLocalDate()}"
        cachePort.increment(failedKey, 1)
            .subscribe(
                { count ->
                    log.info("Failed login count for ${event.email}: $count")
                    if (count >= 5) {
                        log.warn("Multiple failed login attempts detected for ${event.email}")
                    }
                },
                { error -> log.error("Failed to increment failed login count", error) }
            )
    }

    @EventListener
    @Async
    fun handleTwoFactorEnabled(event: TwoFactorEnabledEvent) {
        log.info("2FA enabled: ${event.userId}, ${event.email}")

        emailPort.sendTwoFactorSetupEmail(event.email, "User", "secret")
            .subscribe(
                { log.info("2FA setup email sent to ${event.email}") },
                { error -> log.error("Failed to send 2FA setup email to ${event.email}", error) }
            )
    }

    @EventListener
    @Async
    fun handleSuspiciousActivity(event: SuspiciousActivityEvent) {
        log.warn("Suspicious activity detected: ${event.userId}, ${event.email}, type: ${event.activityType}")

        emailPort.sendSecurityAlertEmail(event.email ?: "", "User", event.description)
            .subscribe(
                { log.info("Security alert email sent to ${event.email}") },
                { error -> log.error("Failed to send security alert email to ${event.email}", error) }
            )
    }
}