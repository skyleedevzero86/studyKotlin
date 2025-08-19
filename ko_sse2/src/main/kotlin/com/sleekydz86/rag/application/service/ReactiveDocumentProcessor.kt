package com.sleekydz86.rag.application.service

import com.sleekydz86.rag.shared.factory.TextSplitterFactory
import org.slf4j.LoggerFactory
import org.springframework.ai.document.Document
import org.springframework.ai.reader.TextReader
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component

@Component
class SimpleDocumentProcessor(
    private val vectorStore: VectorStore,
    private val textSplitterFactory: TextSplitterFactory
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun processDocument(resource: Resource, fileName: String): Result<String> = try {
        val reader = TextReader(resource)
        reader.customMetadata["fileName"] = fileName
        val documents = reader.get()

        val chunks = mutableListOf<Document>()
        documents.forEach { document ->
            val textChunks = textSplitterFactory
                .createSplitter(TextSplitterFactory.SplitterType.CUSTOM)
                .split(document.content)

            textChunks.forEach { chunk ->
                chunks.add(Document(chunk, mapOf("fileName" to fileName)))
            }
        }

        vectorStore.add(chunks)
        logger.info("문서 '$fileName' 처리 완료: ${chunks.size}개 청크")
        Result.success("처리 완료: ${chunks.size}개 청크")

    } catch (e: Exception) {
        logger.error("문서 처리 중 오류: ${e.message}", e)
        Result.failure(e)
    }
}