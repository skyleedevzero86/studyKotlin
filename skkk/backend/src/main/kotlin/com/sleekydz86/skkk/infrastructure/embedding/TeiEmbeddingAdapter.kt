package com.sleekydz86.skkk.infrastructure.embedding

import com.sleekydz86.skkk.domain.port.EmbeddingPort
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class TeiEmbeddingAdapter(
    @Qualifier("embeddingWebClient") private val webClient: WebClient,
    @Value("\${embedding.search-task:Given a web search query, retrieve relevant passages that answer the query}") private val searchTask: String
) : EmbeddingPort {

    override fun embed(text: String): List<Float> = embed(listOf(text)).first()

    override fun embed(texts: List<String>): List<List<Float>> {
        if (texts.isEmpty()) return emptyList()
        val result = webClient.post()
            .uri("/embed")
            .bodyValue(mapOf("inputs" to texts))
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<List<List<Double>>>() {})
            .block() ?: return emptyList()
        return result.map { vec -> vec.map { it.toFloat() } }
    }

    override fun embedQuery(query: String, task: String): List<Float> {
        val instructed = "Instruct: $task\nQuery: $query"
        return embed(instructed)
    }
}
