package com.sleekydz86.skkk.application

import com.sleekydz86.skkk.domain.model.WebPage
import com.sleekydz86.skkk.domain.port.EmbeddingPort
import com.sleekydz86.skkk.domain.port.SiteFetchPort
import com.sleekydz86.skkk.domain.port.VectorStorePort
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class IngestPostUseCase(
    private val siteFetchPort: SiteFetchPort,
    private val embeddingPort: EmbeddingPort,
    private val vectorStorePort: VectorStorePort,
    @Value("\${chroma.collection-name:documents}") private val collectionName: String
) {

    private var collectionId: String? = null

    private fun ensureCollectionId(): String =
        collectionId ?: vectorStorePort.getOrCreateCollectionId(collectionName).also { collectionId = it }

    fun executeFromUrl(url: String, id: String? = null): Result<Unit> = runCatching {
        val docId = id ?: UUID.nameUUIDFromBytes(url.toByteArray()).toString()
        val page = siteFetchPort.fetchPage(url, docId) ?: throw NoSuchElementException("수집 실패: $url")
        addPage(page)
    }

    fun executeFromPage(page: WebPage): Result<Unit> = runCatching {
        addPage(page)
    }

    private fun addPage(page: WebPage) {
        val content = page.toSearchableContent()
        val embedding = embeddingPort.embed(content)
        val metadata = mapOf<String, Any>(
            "title" to page.title,
            "url" to page.url,
            "summary" to (page.summary ?: page.content.take(300)),
            "publishedAt" to (page.publishedAt ?: "")
        )
        vectorStorePort.add(
            collectionId = ensureCollectionId(),
            ids = listOf(page.id),
            embeddings = listOf(embedding),
            documents = listOf(content),
            metadatas = listOf(metadata)
        )
    }
}
