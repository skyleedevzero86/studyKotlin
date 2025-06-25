package com.sleekydz86.slacks.service

import com.slack.api.Slack
import com.slack.api.methods.request.chat.ChatPostMessageRequest
import com.slack.api.model.block.Blocks.* // Blocks 유틸리티 클래스의 모든 정적 메서드를 임포트
import com.slack.api.model.block.element.BlockElements.* // BlockElements 유틸리티 클래스의 모든 정적 메서드를 임포트
import com.slack.api.model.block.element.ButtonElement // ButtonElement 직접 임포트
import com.slack.api.model.block.composition.MarkdownTextObject // MarkdownTextObject 직접 임포트
import com.slack.api.model.block.composition.PlainTextObject // PlainTextObject 직접 임포트
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import org.springframework.web.client.RestTemplate

@Service
class SlackService(
    @Value("\${slack.webhook-uri}") private val webhookUri: String
) {
    private val restTemplate = RestTemplate()

    fun sendSimpleMessage(message: String): Result<Unit> {
        val payload = mapOf("text" to message)
        return try {
            restTemplate.postForEntity(webhookUri, payload, String::class.java)
            println("Slack message sent successfully via webhook: $message")
            Result.success(Unit)
        } catch (e: Exception) {
            println("Error sending Slack webhook message: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun sendServerStatusAlert(): Result<Unit> {
        val message = "🚨 서버 상태 경고: CPU 사용량이 급증했습니다!"
        return sendSimpleMessage(message)
    }

    fun sendNewUserSignupNotification(username: String): Result<Unit> {
        val message = "*새로운 사용자 가입 알림!* :sparkles:\nUser: `$username` 님이 가입했습니다.\n[사용자 프로필 보기](https://your-app.com/users/$username)"
        return sendSimpleMessage(message)
    }
}