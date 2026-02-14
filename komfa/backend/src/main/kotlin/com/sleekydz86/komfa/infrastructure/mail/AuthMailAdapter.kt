package com.sleekydz86.komfa.infrastructure.mail

import com.sleekydz86.komfa.application.user.AuthMailPort
import jakarta.mail.MessagingException
import jakarta.mail.internet.MimeMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component

@Component
class AuthMailAdapter(
    private val mailSender: JavaMailSender,
    @Value("\${komfa.mail.from:}") private val from: String,
    @Value("\${spring.mail.username:}") private val username: String,
) : AuthMailPort {

    private fun fromAddress(): String = from.ifBlank { username }

    override fun sendNewSignupNotificationToAdmin(adminEmail: String, username: String): Boolean {
        val html = """
            <html>
              <body style="font-family: sans-serif;">
                <h1>Komfa 회원 가입 신청</h1>
                <p>회원 <b>$username</b>님이 가입 신청했습니다.</p>
                <p>관리자 페이지에서 승인 후 로그인할 수 있습니다.</p>
              </body>
            </html>
        """.trimIndent()
        return sendHtmlMail(adminEmail, "Komfa - 회원 가입 신청 알림", html)
    }

    override fun sendFindUsernameEmail(to: String, username: String): Boolean {
        val html = """
            <html>
              <body style="font-family: sans-serif;">
                <h1>Komfa 아이디 안내</h1>
                <p>요청하신 아이디는 <b>$username</b> 입니다.</p>
              </body>
            </html>
        """.trimIndent()
        return sendHtmlMail(to, "Komfa - 아이디 안내", html)
    }

    override fun sendResetPasswordEmail(to: String, resetLink: String): Boolean {
        val html = """
            <html>
              <body style="font-family: sans-serif;">
                <h1>Komfa 비밀번호 재설정</h1>
                <p>아래 링크로 접속하여 비밀번호를 변경해 주세요.</p>
                <p><a href="$resetLink">비밀번호 재설정하기</a></p>
                <p style="color:#666;font-size:12px;">링크 유효시간은 60분입니다.</p>
              </body>
            </html>
        """.trimIndent()
        return sendHtmlMail(to, "Komfa - 비밀번호 재설정", html)
    }

    override fun sendPasswordChangeNoticeEmail(to: String, username: String): Boolean {
        val html = """
            <html>
              <body style="font-family: sans-serif;">
                <h1>Komfa 비밀번호 변경 안내</h1>
                <p><b>$username</b>님, 비밀번호가 변경되었습니다.</p>
                <p>본인이 요청한 변경이 아닌 경우 즉시 고객센터에 연락해 주세요.</p>
              </body>
            </html>
        """.trimIndent()
        return sendHtmlMail(to, "Komfa - 비밀번호 변경 안내", html)
    }

    private fun sendHtmlMail(to: String, subject: String, html: String): Boolean {
        return try {
            val message: MimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")
            helper.setFrom(fromAddress())
            helper.setTo(to)
            helper.setSubject(subject)
            helper.setText(html, true)
            mailSender.send(message)
            true
        } catch (_: MessagingException) {
            false
        }
    }
}
