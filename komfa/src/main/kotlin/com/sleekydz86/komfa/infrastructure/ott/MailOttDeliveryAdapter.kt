package com.sleekydz86.komfa.infrastructure.ott

import com.sleekydz86.komfa.application.auth.OttDeliveryPort
import com.sleekydz86.komfa.domain.auth.OttDeliveryResult
import com.sleekydz86.komfa.domain.auth.TokenValue
import com.sleekydz86.komfa.domain.auth.Username
import com.sleekydz86.komfa.infrastructure.crypto.Aes256Service
import com.sleekydz86.komfa.infrastructure.persistence.UserRepository
import jakarta.mail.MessagingException
import jakarta.mail.internet.MimeMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["komfa.mail.enabled"], havingValue = "true")
class MailOttDeliveryAdapter(
    private val mailSender: JavaMailSender,
    private val userRepository: UserRepository,
    private val aes256Service: Aes256Service,
    @Value("\${komfa.ott.magic-link-base-url:http://localhost:8080}") private val baseUrl: String,
    @Value("\${komfa.mail.from:}") private val from: String,
    @Value("\${spring.mail.username:}") private val smtpUsername: String,
) : OttDeliveryPort {

    private fun fromAddress(): String = from.ifBlank { smtpUsername }

    override fun deliver(username: Username, token: TokenValue): OttDeliveryResult {
        val user = userRepository.findByUsername(username.value) ?: return OttDeliveryResult.Failed("사용자를 찾을 수 없습니다.")
        val email = user.email?.let { aes256Service.decryptOrPlain(it) }?.takeIf { it.isNotBlank() }
            ?: return OttDeliveryResult.Failed("사용자 이메일이 등록되지 않았습니다.")
        val magicLink = "$baseUrl/login/ott?token=${token.value}"
        val html = """
            <html>
              <body style="font-family: sans-serif;">
                <h1>Komfa 일회용 토큰</h1>
                <p>아래 링크로 접속하여 로그인을 완료해 주세요.</p>
                <p><a href="$magicLink">로그인하기</a></p>
              </body>
            </html>
        """.trimIndent()
        return try {
            val message: MimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")
            helper.setFrom(fromAddress())
            helper.setTo(email)
            helper.setSubject("Komfa 일회용 토큰")
            helper.setText(html, true)
            mailSender.send(message)
            OttDeliveryResult.Sent
        } catch (e: MessagingException) {
            OttDeliveryResult.Failed(e.message ?: "메일 발송에 실패했습니다.")
        }
    }
}
