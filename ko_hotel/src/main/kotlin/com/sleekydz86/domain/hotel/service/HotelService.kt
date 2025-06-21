package com.sleekydz86.domain.hotel.service

import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Service

@Service
class HotelService(
    private val vectorStore: VectorStore
) {

    fun searchRelevantInfo(query: String, maxResults: Int = 3): List<Document> {
        return try {
            vectorStore.similaritySearch(SearchRequest.builder().query(query).topK(maxResults).build()).orEmpty()
        } catch (e: Exception) {
            println("Search error: ${e.message}")
            emptyList()
        }
    }

    fun addHotelDocument(content: String) {
        val document = Document(content)
        vectorStore.add(listOf(document))
    }
}