package com.sleekydz86.densevector2.support

import com.sleekydz86.densevector2.global.elasticsearch.IndexedPassage
import com.sleekydz86.densevector2.global.elasticsearch.PassageVectorWriter
import com.sleekydz86.densevector2.global.error.VectorStudyException

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
