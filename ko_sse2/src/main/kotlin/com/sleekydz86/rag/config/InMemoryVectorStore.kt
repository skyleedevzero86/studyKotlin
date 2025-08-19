package com.sleekydz86.rag.config

import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.ai.document.Document
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

class InMemoryVectorStore : VectorStore {
    private val documents = ConcurrentHashMap<String, Document>()
    private var nextId = 1

    override fun add(documents: List<Document>) {
        documents.forEach { doc ->
            val id = "doc_${nextId++}"
            this.documents[id] = Document(doc.content, doc.metadata + mapOf("id" to id))
        }
        println("문서 ${documents.size}개가 추가되었습니다.")
    }

    override fun similaritySearch(searchRequest: SearchRequest): List<Document> {
        val query = searchRequest.query
        val topK = searchRequest.topK ?: 5
        val threshold = searchRequest.similarityThreshold ?: 0.7

        val results = this.documents.values
            .filter { doc ->
                val content = doc.content.lowercase()
                val queryWords = query.lowercase().split(" ")
                queryWords.any { word -> content.contains(word) }
            }
            .take(topK)
            .toList()

        println("검색 쿼리: '$query' -> ${results.size}개 결과")
        return results
    }

    override fun similaritySearch(query: String): List<Document> {
        return similaritySearch(SearchRequest.query(query))
    }

    override fun delete(idList: List<String>): Optional<Boolean> {
        idList.forEach { id -> documents.remove(id) }
        return Optional.of(true)
    }
}