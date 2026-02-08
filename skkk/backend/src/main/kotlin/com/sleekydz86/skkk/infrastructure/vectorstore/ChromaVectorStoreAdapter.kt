package com.sleekydz86.skkk.infrastructure.vectorstore

import com.sleekydz86.skkk.domain.port.VectorStorePort
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class ChromaVectorStoreAdapter(
    @Qualifier("chromaWebClient") private val webClient: WebClient,
    @Value("\${chroma.tenant}") private val tenant: String,
    @Value("\${chroma.database}") private val database: String
) : VectorStorePort {

    override fun listCollections(): List<Map<String, Any>> =
        webClient.get()
            .uri("/tenants/{tenant}/databases/{database}/collections", tenant, database)
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<List<Map<String, Any>>>() {})
            .block() ?: emptyList()

    override fun createCollection(name: String): Map<String, Any> =
        webClient.post()
            .uri("/tenants/{tenant}/databases/{database}/collections", tenant, database)
            .bodyValue(mapOf("name" to name))
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
            .block() ?: emptyMap()

    override fun deleteCollection(collectionId: String) {
        webClient.delete()
            .uri("/tenants/{tenant}/databases/{database}/collections/{id}", tenant, database, collectionId)
            .retrieve()
            .toBodilessEntity()
            .block()
    }

    override fun add(
        collectionId: String,
        ids: List<String>,
        embeddings: List<List<Float>>,
        documents: List<String>,
        metadatas: List<Map<String, Any>>?
    ) {
        val body = mutableMapOf<String, Any>(
            "ids" to ids,
            "embeddings" to embeddings,
            "documents" to documents
        )
        if (!metadatas.isNullOrEmpty()) body["metadatas"] = metadatas
        webClient.post()
            .uri("/tenants/{tenant}/databases/{database}/collections/{id}/add", tenant, database, collectionId)
            .bodyValue(body)
            .retrieve()
            .toBodilessEntity()
            .block()
    }

    override fun get(collectionId: String, include: List<String>): Map<String, Any> =
        webClient.post()
            .uri("/tenants/{tenant}/databases/{database}/collections/{id}/get", tenant, database, collectionId)
            .bodyValue(mapOf("include" to include))
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
            .block() ?: emptyMap()

    override fun delete(collectionId: String, ids: List<String>) {
        webClient.post()
            .uri("/tenants/{tenant}/databases/{database}/collections/{id}/delete", tenant, database, collectionId)
            .bodyValue(mapOf("ids" to ids))
            .retrieve()
            .toBodilessEntity()
            .block()
    }

    override fun query(
        collectionId: String,
        queryEmbedding: List<Float>,
        nResults: Int,
        include: List<String>
    ): Map<String, Any> =
        webClient.post()
            .uri("/tenants/{tenant}/databases/{database}/collections/{id}/query", tenant, database, collectionId)
            .bodyValue(mapOf(
                "query_embeddings" to listOf(queryEmbedding),
                "n_results" to nResults,
                "include" to include
            ))
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
            .block() ?: emptyMap()

    override fun getOrCreateCollectionId(name: String): String {
        val list = listCollections()
        @Suppress("UNCHECKED_CAST")
        val found = list.firstOrNull { (it["name"] as? String) == name }
        if (found != null) return (found["id"] as? String) ?: error("컬렉션 ID가 없습니다")
        val created = createCollection(name)
        return (created["id"] as? String) ?: error("컬렉션 생성 후 ID를 반환하지 않았습니다")
    }
}
