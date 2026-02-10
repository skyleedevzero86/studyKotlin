package com.sleekydz86.skkk.infrastructure.embedding

import com.sleekydz86.skkk.domain.port.EmbeddingPort
import com.sleekydz86.skkk.global.error.DomainError
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientException
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class TeiEmbeddingAdapter(
    @Qualifier("embeddingWebClient") private val webClient: WebClient,
    @Value("\${embedding.search-task:Given a web search query, retrieve relevant passages that answer the query}") private val searchTask: String,
    @Value("\${embedding.host}") private val embeddingHost: String
) : EmbeddingPort {

    override fun embed(text: String): List<Float> = embed(listOf(text)).first()

    override fun embed(texts: List<String>): List<List<Float>> {
        if (texts.isEmpty()) return emptyList()
        return try {
            val result = webClient.post()
                .uri("/embed")
                .bodyValue(mapOf("inputs" to texts))
                .retrieve()
                .bodyToMono(object : ParameterizedTypeReference<List<List<Double>>>() {})
                .block() ?: throw DomainError.EmbeddingFailed(
                    cause = IllegalStateException("임베딩 서비스 응답이 비어있습니다")
                )
            result.map { vec -> vec.map { it.toFloat() } }
        } catch (e: WebClientException) {
            throw DomainError.EmbeddingFailed(
                cause = IllegalStateException("임베딩 서비스 연결 실패: $embeddingHost. 서비스가 실행 중인지 확인해주세요.", e)
            )
        } catch (e: Exception) {
            if (e is DomainError.EmbeddingFailed) throw e
            throw DomainError.EmbeddingFailed(
                cause = IllegalStateException("임베딩 생성 중 오류 발생: ${e.message}", e)
            )
        }
    }

    override fun embedQuery(query: String, task: String): List<Float> {
        val instructed = "Instruct: $task\nQuery: $query"
        return embed(instructed)
    }
}
