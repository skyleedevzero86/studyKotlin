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

@Service
class SlackService(
    @Value("\${slack.bot-token}") private val botToken: String,
    @Value("\${slack.channel}") private val defaultChannel: String
) {
    private val slack = Slack.getInstance()
    private val methods = slack.methods(botToken)

    // 간단 메시지 전송 (chat.postMessage 사용)
    fun sendSimpleMessage(message: String) {
        val request = ChatPostMessageRequest.builder()
            .channel(defaultChannel)
            .text(message)
            .build()
        try {
            val response = methods.chatPostMessage(request)
            if (!response.isOk) {
                println("Failed to send Slack message: ${response.error}")
            } else {
                println("Slack message sent successfully: $message")
            }
        } catch (e: Exception) {
            println("Error sending Slack message: ${e.message}")
            e.printStackTrace() // 스택 트레이스도 함께 출력하여 디버깅에 도움
        }
    }

    // 미리 정의된 서버 상태 알림 전송
    fun sendServerStatusAlert() {
        val message = "🚨 서버 상태 경고: CPU 사용량이 급증했습니다!"
        sendSimpleMessage(message) // 간단 메시지 전송 기능을 재활용
    }

    // Block Kit을 사용한 풍부한 알림 호출 예시
    fun sendNewUserSignupNotification(username: String) {
        val blocks = listOf(
            section { // section 빌더 시작
                // 람다의 마지막 표현식이 Unit이 아닌 MarkdownTextObject를 반환하도록 수정
                // text() 메서드를 사용하여 TextObject를 설정
                it.text(MarkdownTextObject.builder().text("*새로운 사용자 가입 알림!* :sparkles:\nUser: `$username` 님이 가입했습니다.").build())
            },
            divider(),
            actions { // actions 빌더 시작
                // elements() 메서드는 List<BlockElement>를 받으므로, listOf()로 감싸줍니다.
                it.elements(
                    listOf(
                        button { // button 빌더 시작
                            // text() 메서드를 사용하여 PlainTextObject를 설정
                            it.text(PlainTextObject.builder().text("사용자 프로필 보기").build())
                                .url("https://your-app.com/users/$username")
                        }
                    )
                )
            }
        )

        val request = ChatPostMessageRequest.builder()
            .channel(defaultChannel)
            .blocks(blocks) // blocks 필드에 Block Kit 객체 전달
            .build()

        try {
            val response = methods.chatPostMessage(request)
            if (!response.isOk) {
                println("Failed to send Slack message: ${response.error}")
            } else {
                println("Slack Block Kit message sent successfully for user: $username")
            }
        } catch (e: Exception) {
            println("Error sending Slack Block Kit message: ${e.message}")
            e.printStackTrace() // 스택 트레이스도 함께 출력하여 디버깅에 도움
        }
    }
}