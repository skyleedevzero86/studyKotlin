package com.sleekydz86.densevector.support

import com.sleekydz86.densevector.global.elasticsearch.IndexDescription
import com.sleekydz86.densevector.global.elasticsearch.IndexedPassage
import com.sleekydz86.densevector.global.elasticsearch.PassageVectorReader
import com.sleekydz86.densevector.global.elasticsearch.PassageVectorWriter
import com.sleekydz86.densevector.global.elasticsearch.VectorHit
import com.sleekydz86.densevector.global.elasticsearch.VectorIndexAdmin
import com.sleekydz86.densevector.global.error.VectorStudyException

class RecordingVectorWriter : PassageVectorWriter {
    val upserts = mutableListOf<IndexedPassage>()
    val deleted = mutableListOf<Long>()
    var error: VectorStudyException? = null

    override fun upsert(id: Long, title: String, body: String, embedding: FloatArray) {
        error?.let { throw it }
        upserts.removeIf { it.id == id }
        upserts += IndexedPassage(id, title, body, embedding)
    }

    override fun bulkUpsert(items: List<IndexedPassage>) {
        error?.let { throw it }
        items.forEach { item -> upsert(item.id, item.title, item.body, item.embedding) }
    }

    override fun delete(id: Long) {
        deleted += id
        upserts.removeIf { it.id == id }
    }
}

class RecordingVectorReader : PassageVectorReader {
    var hits: List<VectorHit> = emptyList()

    override fun search(query: FloatArray, k: Int): List<VectorHit> = hits.take(k)
}

class RecordingIndexAdmin : VectorIndexAdmin {
    var ensured = false

    override fun ensureIndex() {
        ensured = true
    }

    override fun describe(): IndexDescription = IndexDescription(
        index = "test-passages",
        dims = 64,
        similarity = "cosine",
        indexType = "hnsw",
        m = 16,
        efConstruction = 100,
        clusterVersion = "8.15.3"
    )
}
