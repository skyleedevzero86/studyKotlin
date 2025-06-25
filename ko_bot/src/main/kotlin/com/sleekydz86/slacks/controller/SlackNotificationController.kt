package com.sleekydz86.slacks.controller

import com.sleekydz86.slacks.service.SlackService // SlackService를 임포트합니다.
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class SlackNotificationController(
    private val slackService: SlackService // SlackWebhookService 대신 SlackService를 주입받습니다.
) {

    /**
     * 테스트용 Slack 알림을 전송하는 엔드포인트 (간단 메시지)
     * http://localhost:8080/send-test-slack-alert 로 접속하여 테스트합니다.
     */
    @GetMapping("/send-test-slack-alert")
    fun sendTestAlert(): String {
        val message = "🌟 Spring Boot 앱에서 보낸 테스트 알림입니다!"
        slackService.sendSimpleMessage(message) // SlackWebhookService.sendSlackMessage 대신 SlackService.sendSimpleMessage 호출
        println("Slack test alert sent: $message")
        return "Slack 테스트 알림이 성공적으로 전송되었습니다: '$message'"
    }

    /**
     * 특정 메시지를 받아 Slack으로 전송하는 엔드포인트 (간단 메시지)
     * 예시: http://localhost:8080/send-custom-slack-alert?msg=안녕하세요_Slack_알림입니다!
     */
    @GetMapping("/send-custom-slack-alert")
    fun sendCustomAlert(@RequestParam msg: String): String {
        slackService.sendSimpleMessage(msg) // SlackWebhookService.sendSlackMessage 대신 SlackService.sendSimpleMessage 호출
        println("Slack custom alert sent: $msg")
        return "Slack 커스텀 알림이 성공적으로 전송되었습니다: '$msg'"
    }

    /**
     * SlackService 내의 미리 정의된 알림 메서드 호출 예시
     * 이 메서드는 SlackService에서 미리 정의된 메시지를 보냅니다.
     */
    @GetMapping("/send-predefined-server-alert")
    fun sendPredefinedServerAlert(): String {
        slackService.sendServerStatusAlert() // SlackWebhookService.sendAlert 대신 SlackService.sendServerStatusAlert 호출
        println("Slack predefined server status alert sent.")
        return "미리 정의된 서버 상태 Slack 알림이 전송되었습니다."
    }

    /**
     * SlackService 내의 Block Kit을 사용한 풍부한 알림 호출 예시
     */
    @GetMapping("/send-rich-signup-alert")
    fun sendRichSignupAlert(@RequestParam username: String): String {
        slackService.sendNewUserSignupNotification(username)
        println("Slack rich signup notification sent for user: $username")
        return "새로운 사용자 가입 알림이 전송되었습니다."
    }
}