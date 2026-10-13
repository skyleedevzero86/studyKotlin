package com.sleekydz86.densevector.global.elasticsearch

interface PassageVectorWriter {
    fun upsert(id: Long, title: String, body: String, embedding: FloatArray)
    fun bulkUpsert(items: List<IndexedPassage>)
    fun delete(id: Long)
}
