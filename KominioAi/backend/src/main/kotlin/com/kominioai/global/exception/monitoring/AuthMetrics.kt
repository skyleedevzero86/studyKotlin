package com.kominioai.global.exception.monitoring

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class AuthMetrics(
    private val meterRegistry: MeterRegistry
) {
    private val loginSuccessCounter = Counter.builder("auth.login.success")
        .description("Successful login attempts")
        .register(meterRegistry)

    private val loginFailureCounter = Counter.builder("auth.login.failure")
        .description("Failed login attempts")
        .register(meterRegistry)

    private val registrationCounter = Counter.builder("auth.registration")
        .description("User registrations")
        .register(meterRegistry)

    private val loginTimer = Timer.builder("auth.login.duration")
        .description("Login request duration")
        .register(meterRegistry)

    fun recordLoginSuccess() {
        loginSuccessCounter.increment()
    }

    fun recordLoginFailure() {
        loginFailureCounter.increment()
    }

    fun recordRegistration() {
        registrationCounter.increment()
    }

    fun recordLoginTime(duration: Duration) {
        loginTimer.record(duration)
    }
}