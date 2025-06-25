package com.sleekydz86.discod.service

import com.sleekydz86.discod.entity.DiscordWebhookMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import mu.KotlinLogging // Kotlin Logging 라이브러리 사용을 가정
import org.springframework.http.ResponseEntity

@Service
class MessageService {

    // Kotlin Logging 로거 인스턴스
    private val log = KotlinLogging.logger {}

    // application.yml에 설정된 Discord 웹훅 URL을 주입받습니다.
    @Value("\${logging.discord.webhook-url}")
    private lateinit var discordWebhookUrl: String

    /**
     * Discord 웹훅 메시지를 전송합니다.
     * @param message 전송할 DiscordWebhookMessage 객체
     */
    fun sendDiscordWebhookMessage(message: DiscordWebhookMessage) {
        try {
            // HTTP 헤더 설정: Content-Type을 application/json으로 지정합니다.
            val httpHeaders = HttpHeaders().apply {
                add("Content-Type", "application/json; utf-8")
            }

            // 메시지 본문과 헤더를 포함하는 HttpEntity를 생성합니다.
            val messageEntity = HttpEntity(message, httpHeaders)

            // RestTemplate을 사용하여 HTTP POST 요청을 보냅니다.
            val template = RestTemplate()
            val response: ResponseEntity<String> = template.exchange(
                discordWebhookUrl, // 웹훅 URL
                HttpMethod.POST,   // HTTP 메서드: POST
                messageEntity,     // 요청 엔티티 (본문 + 헤더)
                String::class.java // 응답 타입 (Kotlin에서 Java Class 객체 접근)
            )

            // 응답 상태 코드가 204 No Content가 아니면 에러로 간주하고 로깅합니다.
            if (response.statusCode != HttpStatus.NO_CONTENT) {
                log.error("메시지 전송 이후 에러 발생. 응답 코드: {}", response.statusCode.value())
                log.error("응답 본문: {}", response.body)
            } else {
                log.info("Discord 웹훅 메시지 전송 성공.")
            }

        } catch (e: Exception) {
            // 예외 발생 시 에러 로깅
            log.error("Discord 웹훅 메시지 전송 중 에러 발생 :: ${e.message}", e)
        }
    }
}
