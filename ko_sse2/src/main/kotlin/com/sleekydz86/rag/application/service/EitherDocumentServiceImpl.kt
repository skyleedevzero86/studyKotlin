package com.sleekydz86.rag.application.service

import com.sleekydz86.rag.common.functional.monad.Either
import com.sleekydz86.rag.shared.factory.TextSplitterFactory
import org.slf4j.LoggerFactory
import org.springframework.ai.document.Document
import org.springframework.ai.reader.TextReader
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service


@Service
class EitherDocumentServiceImpl(
    private val vectorStore: VectorStore,
    private val textSplitterFactory: TextSplitterFactory
) : EitherDocumentService {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun loadText(resource: Resource, fileName: String): Either<String, Unit> = try {
        val reader = TextReader(resource)
        reader.customMetadata["fileName"] = fileName
        val documents = reader.get()

        val processedDocuments = documents.flatMap { document ->
            textSplitterFactory
                .createSplitter(TextSplitterFactory.SplitterType.CUSTOM)
                .split(document.content)
                .map { chunk -> Document(chunk, mapOf("fileName" to fileName)) }
        }

        vectorStore.add(processedDocuments)
        logger.info("문서 '$fileName' 처리 완료: ${processedDocuments.size}개 청크")

        Either.Right(Unit)

    } catch (e: Exception) {
        logger.error("문서 로드 실패: ${e.message}", e)
        Either.Left("문서 로드 실패: ${e.message}")
    }

    override fun doSearch(query: String): Either<String, List<Document>> = try {
        val results = vectorStore.similaritySearch(query)
        logger.info("검색 완료: '$query' -> ${results.size}개 결과")
        Either.Right(results)

    } catch (e: Exception) {
        logger.error("검색 실패: ${e.message}", e)
        Either.Left("검색 실패: ${e.message}")
    }
}
