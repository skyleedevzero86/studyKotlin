package com.sleekydz86.skkk.application

import com.sleekydz86.skkk.domain.model.SearchResult
import com.sleekydz86.skkk.domain.port.EmbeddingPort
import com.sleekydz86.skkk.domain.port.VectorStorePort
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SearchBlogUseCase(
    private val embeddingPort: EmbeddingPort,
    private val vectorStorePort: VectorStorePort,
    @Value("\${chroma.collection-name:documents}") private val collectionName: String
) {

    private var collectionId: String? = null

    private fun ensureCollectionId(): String =
        collectionId ?: vectorStorePort.getOrCreateCollectionId(collectionName).also { collectionId = it }

    fun execute(query: String, topK: Int, collectionId: String? = null): Result<List<SearchResult>> = runCatching {
        val q = query.trim().takeIf { it.isNotBlank() } ?: throw IllegalArgumentException("검색어가 비어 있습니다")
        val k = topK.coerceIn(1, 20)
        val cid = collectionId ?: ensureCollectionId()
        val queryEmbedding = embeddingPort.embedQuery(q, defaultSearchTask)
        val raw = vectorStorePort.query(
            collectionId = cid,
            queryEmbedding = queryEmbedding,
            nResults = k,
            include = listOf("documents", "metadatas", "distances")
        )
        parseQueryResponse(raw)
    }

    private fun parseQueryResponse(raw: Map<String, Any>): List<SearchResult> {
        val ids = (raw["ids"] as? List<*>)?.firstOrNull() as? List<*> ?: return emptyList()
        val metadatas = (raw["metadatas"] as? List<*>)?.firstOrNull() as? List<*> ?: return emptyList()
        val documents = (raw["documents"] as? List<*>)?.firstOrNull() as? List<*> ?: return emptyList()
        val distances = (raw["distances"] as? List<*>)?.firstOrNull() as? List<*>
        return ids.mapIndexed { i, id ->
            val meta = (metadatas.getOrNull(i) as? Map<*, *>)?.mapKeys { it.key?.toString() ?: "" }?.mapValues { it.value?.toString() ?: "" } ?: emptyMap()
            val doc = documents.getOrNull(i) as? String ?: ""
            val dist = distances?.getOrNull(i) as? Double
            SearchResult(
                id = id?.toString() ?: "",
                title = meta["title"] ?: "",
                url = meta["url"] ?: "",
                summary = meta["summary"] ?: doc.take(200),
                score = dist
            )
        }
    }

    companion object {
        private const val defaultSearchTask = "Given a web search query, retrieve relevant passages that answer the query"
    }
}
