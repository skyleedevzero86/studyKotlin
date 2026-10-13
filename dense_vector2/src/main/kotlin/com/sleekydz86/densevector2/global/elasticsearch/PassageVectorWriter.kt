package com.sleekydz86.densevector2.global.elasticsearch

interface PassageVectorWriter {
    fun upsert(id: Long, title: String, body: String, embedding: FloatArray)
    fun bulkUpsert(items: List<IndexedPassage>)
    fun delete(id: Long)
}
