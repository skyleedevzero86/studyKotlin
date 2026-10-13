package com.sleekydz86.densevector2.passage.create

import com.sleekydz86.densevector2.global.elasticsearch.PassageVectorWriter
import com.sleekydz86.densevector2.global.embedding.HashingEmbedder
import com.sleekydz86.densevector2.global.error.VectorStudyException
import com.sleekydz86.densevector2.global.passage.PassageDetail
import com.sleekydz86.densevector2.global.passage.PassageViews
import com.sleekydz86.densevector2.global.persistence.PassageEntity
import com.sleekydz86.densevector2.global.persistence.PassageRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PassageCreateService(
    private val repository: PassageRepository,
    private val embedder: HashingEmbedder,
    private val vectorIndex: PassageVectorWriter
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun create(title: String, body: String): PassageDetail {
        val vector = embedder.embed(body.trim())
        val saved = repository.save(PassageViews.newEntity(title.trim(), body.trim(), vector))
        indexOrRollback(saved, vector)
        return PassageViews.detail(saved)
    }

    private fun indexOrRollback(saved: PassageEntity, vector: FloatArray) {
        val id = PassageViews.idOf(saved)
        try {
            vectorIndex.upsert(id, saved.title, saved.body, vector)
        } catch (ex: VectorStudyException) {
            repository.delete(saved)
            log.error("Elasticsearch 색인 실패로 MySQL 문단을 삭제했습니다. id={}", id, ex)
            throw ex
        }
    }
}
