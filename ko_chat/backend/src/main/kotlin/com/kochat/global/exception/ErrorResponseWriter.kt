package com.kochat.global.exception

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.nio.charset.StandardCharsets

@Component
class ErrorResponseWriter(
    private val objectMapper: ObjectMapper,
) {

    fun write(response: HttpServletResponse, status: Int, body: ApiErrorResponse) {
        response.status = status
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = StandardCharsets.UTF_8.name()
        response.writer.write(objectMapper.writeValueAsString(body))
        response.writer.flush()
    }
}
