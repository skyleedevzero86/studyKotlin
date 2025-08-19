package com.sleekydz86.rag.config

import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.ai.document.Document
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

@Component
class RedisVectorStore(
    private val redisTemplate: RedisTemplate<String, Any>
) : VectorStore {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val documents = ConcurrentHashMap<String, Document>()
    private var nextId = 1

    override fun add(documents: List<Document>) {
        documents.forEach { doc ->
            val id = "doc_${nextId++}"
            this.documents[id] = Document(doc.content, doc.metadata + mapOf("id" to id))

            val key = "document:$id"
            val metadataKey = "metadata:$id"

            try {
                redisTemplate.opsForValue().set(key, doc.content)

                val metadataMap = doc.metadata.mapValues { (_, value) -> value.toString() }
                redisTemplate.opsForHash<String, String>().putAll(metadataKey, metadataMap)

                logger.info("문서 Redis 저장 완료: $key, 메타데이터: $metadataKey")
                logger.debug("저장된 내용: ${doc.content.take(100)}...")

            } catch (e: Exception) {
                logger.error("Redis 저장 실패: $key", e)
            }
        }
        logger.info("Redis VectorStore에 문서 ${documents.size}개가 추가되었습니다.")
    }

    override fun similaritySearch(searchRequest: SearchRequest): List<Document> {
        val query = searchRequest.query
        val topK = searchRequest.topK ?: 5
        val threshold = searchRequest.similarityThreshold ?: 0.1

        logger.info("Redis 검색 시작: '$query' (topK: $topK, threshold: $threshold)")

        val results = this.documents.values
            .mapNotNull { doc ->
                val score = calculateSimilarity(query, doc.content)
                logger.debug("문서 유사도 점수: ${doc.metadata["fileName"]} -> $score")
                if (score >= threshold) {
                    doc to score
                } else null
            }
            .sortedByDescending { it.second }
            .take(topK)
            .map { it.first }
            .toList()

        logger.info("Redis 검색 완료: '$query' -> ${results.size}개 결과 (임계값: $threshold)")
        return results
    }

    override fun similaritySearch(query: String): List<Document> {
        return similaritySearch(SearchRequest.query(query))
    }

    override fun delete(idList: List<String>): Optional<Boolean> {
        idList.forEach { id ->
            documents.remove(id)
            redisTemplate.delete("document:$id")
            redisTemplate.delete("metadata:$id")
        }
        return Optional.of(true)
    }

    private fun calculateSimilarity(query: String, content: String): Double {
        val queryWords = query.lowercase().split(" ").filter { it.length > 2 }
        val contentLower = content.lowercase()

        if (queryWords.isEmpty()) return 0.0

        val matches = queryWords.count { word -> contentLower.contains(word) }
        val score = matches.toDouble() / queryWords.size

        logger.debug("유사도 계산: 쿼리 '$query' -> 문서 '$content' = $score")
        return score
    }

    @Bean
    fun redisHealthCheck(redisTemplate: RedisTemplate<String, Any>): String {
        return try {
            redisTemplate.opsForValue().set("health_check", "ok")
            val result = redisTemplate.opsForValue().get("health_check")
            if (result == "ok") {
                "Redis 연결 성공"
            } else {
                "Redis 연결 실패"
            }
        } catch (e: Exception) {
            "Redis 연결 오류: ${e.message}"
        }
    }
}