package com.sleekydz86.skkk.application

import com.sleekydz86.skkk.domain.model.WebPage
import com.sleekydz86.skkk.domain.port.EmbeddingPort
import com.sleekydz86.skkk.domain.port.SiteFetchPort
import com.sleekydz86.skkk.domain.port.VectorStorePort
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class IngestFeedUseCase(
    private val siteFetchPort: SiteFetchPort,
    private val embeddingPort: EmbeddingPort,
    private val vectorStorePort: VectorStorePort,
    @Value("\${chroma.collection-name:documents}") private val collectionName: String
) {

    private var collectionId: String? = null

    private fun ensureCollectionId(): String =
        collectionId ?: vectorStorePort.getOrCreateCollectionId(collectionName).also { collectionId = it }

    fun execute(feedUrl: String, maxPosts: Int = 50): Result<Int> = runCatching {
        val links = siteFetchPort.extractLinksFromFeed(feedUrl).take(maxPosts)
        val pages = links.mapIndexed { i, url ->
            siteFetchPort.fetchPage(url, "feed-${feedUrl.hashCode()}-$i")
        }.filterNotNull()
        if (pages.isEmpty()) throw NoSuchElementException("피드에서 수집된 포스트가 없습니다")
        addPages(pages)
        pages.size
    }

    private fun addPages(pages: List<WebPage>) {
        val contents = pages.map { it.toSearchableContent() }
        val embeddings = embeddingPort.embed(contents)
        val ids = pages.map { it.id }
        val metadatas = pages.map { page ->
            mapOf<String, Any>(
                "title" to page.title,
                "url" to page.url,
                "summary" to (page.summary ?: page.content.take(300)),
                "publishedAt" to (page.publishedAt ?: "")
            )
        }
        vectorStorePort.add(
            collectionId = ensureCollectionId(),
            ids = ids,
            embeddings = embeddings,
            documents = contents,
            metadatas = metadatas
        )
    }
}
