package com.sleekydz86.slacks.controller

import com.sleekydz86.slacks.service.SlackService // SlackService를 임포트합니다.
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus

@RestController
class SlackNotificationController(
    private val slackService: SlackService
) {
    @GetMapping("/send-test-slack-alert")
    fun sendTestAlert(): ResponseEntity<String> {
        val message = "🌟 Spring Boot 앱에서 보낸 테스트 알림입니다!"
        return slackService.sendSimpleMessage(message).fold(
            onSuccess = {
                println("Slack test alert sent: $message")
                ResponseEntity.ok("Slack 테스트 알림이 성공적으로 전송되었습니다: '$message'")
            },
            onFailure = { e ->
                println("Failed to send Slack test alert: ${e.message}")
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Slack 테스트 알림 전송에 실패했습니다: ${e.message}")
            }
        )
    }

    @GetMapping("/send-custom-slack-alert")
    fun sendCustomAlert(@RequestParam msg: String): ResponseEntity<String> {
        return slackService.sendSimpleMessage(msg).fold(
            onSuccess = {
                println("Slack custom alert sent: $msg")
                ResponseEntity.ok("Slack 커스텀 알림이 성공적으로 전송되었습니다: '$msg'")
            },
            onFailure = { e ->
                println("Failed to send Slack custom alert: ${e.message}")
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Slack 커스텀 알림 전송에 실패했습니다: ${e.message}")
            }
        )
    }

    @GetMapping("/send-predefined-server-alert")
    fun sendPredefinedServerAlert(): ResponseEntity<String> {
        return slackService.sendServerStatusAlert().fold(
            onSuccess = {
                println("Slack predefined server status alert sent.")
                ResponseEntity.ok("미리 정의된 서버 상태 Slack 알림이 전송되었습니다.")
            },
            onFailure = { e ->
                println("Failed to send Slack server status alert: ${e.message}")
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버 상태 알림 전송에 실패했습니다: ${e.message}")
            }
        )
    }

    @GetMapping("/send-rich-signup-alert")
    fun sendRichSignupAlert(@RequestParam username: String): ResponseEntity<String> {
        return slackService.sendNewUserSignupNotification(username).fold(
            onSuccess = {
                println("Slack rich signup notification sent for user: $username")
                ResponseEntity.ok("새로운 사용자 가입 알림이 전송되었습니다.")
            },
            onFailure = { e ->
                println("Failed to send Slack rich signup alert: ${e.message}")
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("사용자 가입 알림 전송에 실패했습니다: ${e.message}")
            }
        )
    }
}