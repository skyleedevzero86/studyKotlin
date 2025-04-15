package com.books.global.config

import com.books.domain.service.BookService
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * MCP 서버 설정 클래스, MCP 도구 등록을 담당함
 */
@Configuration
class McpServerConfig {

    /**
     * 도구 콜백 제공자 등록 - BookService에 정의된 @Tool 메서드를 MCP 도구로 노출함
     *
     * @param bookService 도서 서비스
     * @return 도구 콜백 제공자
     */
    @Bean
    fun bookToolCallbackProvider(bookService: BookService): ToolCallbackProvider =
        MethodToolCallbackProvider.builder()
            .toolObjects(bookService)
            .build()
}
