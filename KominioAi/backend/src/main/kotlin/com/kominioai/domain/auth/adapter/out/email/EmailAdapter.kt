package com.kominioai.domain.auth.adapter.out.email

import com.kominioai.domain.auth.application.port.out.EmailPort
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class EmailAdapter(
    private val mailSender: JavaMailSender
) : EmailPort {
    override fun sendVerificationEmail(to: String, token: String, username: String): Mono<Void> =
        sendMail(to, "Email Verification", "Hello $username, verify: $token")

    override fun sendPasswordResetEmail(to: String, token: String, username: String): Mono<Void> =
        sendMail(to, "Password Reset", "Hello $username, reset: $token")

    override fun sendWelcomeEmail(to: String, username: String): Mono<Void> =
        sendMail(to, "Welcome", "Welcome $username!")

    override fun sendSecurityAlertEmail(to: String, username: String, activity: String): Mono<Void> =
        sendMail(to, "Security Alert", "Hello $username, activity: $activity")

    override fun sendTwoFactorSetupEmail(to: String, username: String, secret: String): Mono<Void> =
        sendMail(to, "2FA Setup", "Hello $username, secret: $secret")

    private fun sendMail(to: String, subject: String, text: String): Mono<Void> = Mono.fromRunnable {
        val message = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, false, "UTF-8")
        helper.setTo(to)
        helper.setSubject(subject)
        helper.setText(text, false)
        mailSender.send(message)
    }
}