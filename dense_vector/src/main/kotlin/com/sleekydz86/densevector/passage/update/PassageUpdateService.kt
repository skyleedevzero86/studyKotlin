package com.sleekydz86.densevector.passage.update

import com.sleekydz86.densevector.global.elasticsearch.PassageVectorWriter
import com.sleekydz86.densevector.global.embedding.HashingEmbedder
import com.sleekydz86.densevector.global.embedding.VectorCodec
import com.sleekydz86.densevector.global.passage.PassageDetail
import com.sleekydz86.densevector.global.passage.PassageViews
import com.sleekydz86.densevector.global.persistence.PassageLookup
import com.sleekydz86.densevector.global.persistence.PassageRepository
import org.springframework.stereotype.Service

@Service
class PassageUpdateService(
    private val repository: PassageRepository,
    private val lookup: PassageLookup,
    private val embedder: HashingEmbedder,
    private val vectorIndex: PassageVectorWriter
) {
    fun update(id: Long, title: String, body: String): PassageDetail {
        val entity = lookup.get(id)
        val vector = embedder.embed(body.trim())
        entity.title = title.trim()
        entity.body = body.trim()
        entity.dims = vector.size
        entity.embeddingJson = VectorCodec.encode(vector)
        val saved = repository.save(entity)
        vectorIndex.upsert(PassageViews.idOf(saved), saved.title, saved.body, vector)
        return PassageViews.detail(saved)
    }
}
