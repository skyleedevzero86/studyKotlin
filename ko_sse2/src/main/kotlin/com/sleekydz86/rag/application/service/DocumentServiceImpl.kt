package com.sleekydz86.rag.application.service

import org.springframework.ai.document.Document
import org.springframework.ai.reader.tika.TikaDocumentReader
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

@Service
class DocumentServiceImpl(
    private val vectorStore: VectorStore
) : DocumentService {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val textSplitter = TokenTextSplitter()

    override fun loadText(resource: Resource, fileName: String): Result<Unit> {
        return try {
            logger.info("문서 로딩 시작: $fileName")

            val documentReader = TikaDocumentReader(resource)
            val documents = documentReader.get()

            val documentsWithMetadata = documents.map { document ->
                Document(
                    document.content,
                    document.metadata + mapOf(
                        "fileName" to fileName,
                        "source" to resource.toString()
                    )
                )
            }

            val splitDocuments = textSplitter.apply(documentsWithMetadata)

            vectorStore.add(splitDocuments)

            logger.info("문서 로딩 완료: $fileName, 분할된 청크 수: ${splitDocuments.size}")
            Result.success(Unit)

        } catch (e: Exception) {
            logger.error("문서 로딩 실패: $fileName", e)
            Result.failure(e)
        }
    }

    override fun doSearch(query: String): List<Document> {
        return try {
            logger.debug("벡터 검색 실행: $query")
            val results = vectorStore.similaritySearch(
                SearchRequest.query(query)
                    .withTopK(5)
                    .withSimilarityThreshold(0.7)
            )
            logger.debug("검색 결과 수: ${results.size}")
            results
        } catch (e: Exception) {
            logger.error("벡터 검색 실패: $query", e)
            emptyList()
        }
    }
}