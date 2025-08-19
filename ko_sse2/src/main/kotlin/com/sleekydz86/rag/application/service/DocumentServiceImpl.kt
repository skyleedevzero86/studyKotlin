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

            logger.info("원본 문서 내용 길이: ${documents.firstOrNull()?.content?.length ?: 0}")

            val documentsWithMetadata = documents.map { document ->
                Document(
                    document.content,
                    document.metadata + mapOf(
                        "fileName" to fileName,
                        "source" to resource.toString(),
                        "timestamp" to System.currentTimeMillis().toString(),
                        "contentLength" to document.content.length.toString()
                    )
                )
            }

            val splitDocuments = textSplitter.apply(documentsWithMetadata)

            logger.info("문서 분할 완료: 원본 ${documents.size}개 -> 분할 ${splitDocuments.size}개")

            splitDocuments.forEachIndexed { index, doc ->
                logger.debug("청크 ${index + 1}: ${doc.content.take(100)}...")
            }

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
            logger.info("벡터 검색 실행: '$query'")

            val searchRequest = SearchRequest.query(query)
                .withTopK(20)
                .withSimilarityThreshold(0.01)

            val results = vectorStore.similaritySearch(searchRequest)

            logger.info("검색 결과 수: ${results.size}")
            results.forEachIndexed { index, doc ->
                val fileName = doc.metadata["fileName"] ?: "알 수 없는 문서"
                val content = doc.content.take(150)
                logger.info("결과 ${index + 1}: [$fileName] - $content...")
            }

            results
        } catch (e: Exception) {
            logger.error("벡터 검색 실패: '$query'", e)
            emptyList()
        }
    }
}