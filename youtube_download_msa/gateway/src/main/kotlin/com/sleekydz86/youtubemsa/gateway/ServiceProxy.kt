package com.sleekydz86.youtubemsa.gateway

import com.sleekydz86.youtubemsa.shared.StudyException
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Component
class ServiceProxy(properties: GatewayProperties) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val clients = mapOf(
        Upstream.DOWNLOAD to RestClient.builder().baseUrl(properties.downloadApi).build(),
        Upstream.SEARCH to RestClient.builder().baseUrl(properties.searchApi).build(),
    )

    fun forward(upstream: Upstream, request: HttpServletRequest, body: ByteArray?): ResponseEntity<ByteArray> {
        val path = request.requestURI + queryOf(request)
        val spec = clients.getValue(upstream).method(org.springframework.http.HttpMethod.valueOf(request.method)).uri(path)
        if (body != null && body.isNotEmpty()) {
            spec.contentType(MediaType.parseMediaType(request.contentType ?: MediaType.APPLICATION_JSON_VALUE))
            spec.body(body)
        }
        return try {
            val response = spec.retrieve().toEntity(ByteArray::class.java)
            ResponseEntity.status(response.statusCode).body(response.body)
        } catch (ex: RestClientResponseException) {
            ResponseEntity.status(ex.statusCode).body(ex.responseBodyAsByteArray)
        } catch (ex: Exception) {
            log.error("게이트웨이 전달 실패 upstream={} path={}", upstream, path, ex)
            throw StudyException("뒤 서비스에 연결하지 못했습니다.", ex, 502)
        }
    }

    private fun queryOf(request: HttpServletRequest): String =
        request.queryString?.let { "?$it" } ?: ""
}
