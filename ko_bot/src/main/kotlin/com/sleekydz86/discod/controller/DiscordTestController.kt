package com.sleekydz86.discod.controller

import com.sleekydz86.discod.entity.Comment
import com.sleekydz86.discod.entity.DiscordWebhookMessage
import com.sleekydz86.discod.entity.Feed
import com.sleekydz86.discod.entity.ReportedType
import com.sleekydz86.discod.entity.User
import com.sleekydz86.discod.service.MessageService
import com.sleekydz86.global.util.MessageFormatter
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class DiscordTestController(
    private val messageService: MessageService
) {

    @GetMapping("/send-test-feed-report")
    fun sendTestFeedReport(): String {
        // 테스트용 데이터 생성
        val user = User(userId = 1L, nickname = "테스트유저123")

        val feed = Feed(feedId = 100L, user = user, feedContent = "이것은 테스트 피드 내용입니다. 비속어가 포함되어 있습니다.")

        // 메시지 포맷팅 (MessageFormatter는 Kotlin으로 변환되지 않았으므로 Java 클래스라고 가정)
        val messageContent = MessageFormatter.formatFeedReportMessage(feed, ReportedType.IMPERTINENCE, 5, true)

        // DiscordWebhookMessage 객체 생성 (Kotlin data class의 named arguments 활용)
        val discordMessage = DiscordWebhookMessage(
            content = messageContent,
            username = "피드 신고 알림 봇" // 웹훅으로 표시될 봇 이름
        )

        // 디스코드 메시지 전송
        messageService.sendDiscordWebhookMessage(discordMessage)

        return "Test feed report message sent to Discord!"
    }

    @GetMapping("/send-test-comment-report")
    fun sendTestCommentReport(): String {
        // 테스트용 데이터 생성
        val user = User(userId = 2L, nickname = "댓글작성자456")

        val comment = Comment(commentId = 200L, user = user, commentContent = "이것은 스포일러 댓글 내용입니다. 영화 결말이 들어있어요!")

        // 메시지 포맷팅 (MessageFormatter는 Kotlin으로 변환되지 않았으므로 Java 클래스라고 가정)
        val messageContent = MessageFormatter.formatCommentReportMessage(comment, ReportedType.SPOILER, user, 3, true)

        // DiscordWebhookMessage 객체 생성 (Kotlin data class의 named arguments 활용)
        val discordMessage = DiscordWebhookMessage(
            content = messageContent,
            username = "댓글 신고 알림 봇" // 웹훅으로 표시될 봇 이름
        )

        // 디스코드 메시지 전송
        messageService.sendDiscordWebhookMessage(discordMessage)

        return "Test comment report message sent to Discord!"
    }
}
