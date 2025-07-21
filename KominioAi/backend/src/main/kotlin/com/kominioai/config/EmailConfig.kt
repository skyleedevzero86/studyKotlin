package com.kominioai.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import java.util.*

@Configuration
class EmailConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(
        prefix = "app.email",
        name = ["mock"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun mockJavaMailSender(): JavaMailSender {
        return object : JavaMailSender {
            override fun createMimeMessage() = JavaMailSenderImpl().createMimeMessage()
            override fun createMimeMessage(contentStream: java.io.InputStream) = JavaMailSenderImpl().createMimeMessage(contentStream)
            override fun send(mimeMessage: jakarta.mail.internet.MimeMessage) {
                println("📧 Mock Email: 이메일이 전송되었습니다")
                println("   - Subject: ${mimeMessage.subject}")
                println("   - To: ${mimeMessage.allRecipients?.joinToString(", ")}")
            }
            override fun send(vararg mimeMessages: jakarta.mail.internet.MimeMessage) {
                mimeMessages.forEach { send(it) }
            }
            override fun send(simpleMessage: org.springframework.mail.SimpleMailMessage) {
                println("📧 Mock Email: ${simpleMessage.to?.joinToString()} - ${simpleMessage.subject}")
                println("   - Text: ${simpleMessage.text}")
            }
            override fun send(vararg simpleMessages: org.springframework.mail.SimpleMailMessage) {
                simpleMessages.forEach { send(it) }
            }
        }
    }

    @Bean
    @ConditionalOnProperty(
        prefix = "app.email",
        name = ["mock"],
        havingValue = "false"
    )
    fun realJavaMailSender(): JavaMailSender {
        val mailSender = JavaMailSenderImpl()

        mailSender.host = "smtp.gmail.com"
        mailSender.port = 587
        mailSender.username = ""
        mailSender.password = ""

        val props = Properties()
        props["mail.transport.protocol"] = "smtp"
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.debug"] = "false"
        props["mail.smtp.ssl.trust"] = "*"
        props["mail.smtp.ssl.enable"] = "false"

        mailSender.javaMailProperties = props

        return mailSender
    }
}