package com.books.global.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.context.annotation.*

/**
 * 채팅 클라이언트 설정 클래스
 */
@Configuration
class ChatClientConfig(
    private val toolCallbackProvider: ToolCallbackProvider
) {

    /**
     * ChatClient를 설정하고, 시스템 프롬프트와 도구 함수를 등록한다.
     */
    @Bean
    fun chatClient(builder: ChatClient.Builder): ChatClient =
        builder
            .defaultSystem(
                """
                당신은 도서 관리 도우미입니다. 사용자가 도서 정보를 조회할 수 있도록 도와주세요.
                도서 제목으로 부분 검색, 저자명으로 검색, 카테고리로 검색이 가능합니다.
                응답 시에는 간결하고 친절한 말투로, 도서 정보를 읽기 쉽게 정리해 주세요.
                """.trimIndent()
            )
            .defaultTools(toolCallbackProvider)
            .build()
}