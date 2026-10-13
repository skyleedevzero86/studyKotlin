package com.sleekydz86.densevector.global.search

import com.sleekydz86.densevector.global.config.VectorProperties
import com.sleekydz86.densevector.global.elasticsearch.PassageVectorReader
import com.sleekydz86.densevector.global.embedding.CosineSimilarity
import com.sleekydz86.densevector.global.embedding.HashingEmbedder
import com.sleekydz86.densevector.global.embedding.SearchLimits
import com.sleekydz86.densevector.global.embedding.VectorCodec
import com.sleekydz86.densevector.global.passage.PassageViews
import com.sleekydz86.densevector.global.persistence.PassageRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PassageSearchService(
    private val repository: PassageRepository,
    private val embedder: HashingEmbedder,
    private val vectorIndex: PassageVectorReader,
    private val properties: VectorProperties
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun search(query: String, k: Int?): SearchResponseBody {
        val limit = SearchLimits.normalizeK(k)
        val queryVector = embedder.embed(query.trim())
        val hits = vectorIndex.search(queryVector, limit).mapNotNull { hit ->
            toHit(hit.passageId, hit.esScore, queryVector)
        }
        return SearchResponseBody(queryVector.size, properties.similarity, limit, hits)
    }

    private fun toHit(id: Long, esScore: Double, queryVector: FloatArray): SearchHitResponse? {
        val entity = repository.findById(id).orElse(null)
        if (entity == null) {
            log.warn("Elasticsearch 히트에 대응하는 MySQL 문단이 없습니다. id={}", id)
            return null
        }
        val cosine = CosineSimilarity.score(queryVector, VectorCodec.decode(entity.embeddingJson))
        return SearchHitResponse(PassageViews.idOf(entity), entity.title, entity.body, esScore, cosine)
    }
}
