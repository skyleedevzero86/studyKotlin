package com.sleekydz86.densevector2.global.elasticsearch

import com.sleekydz86.densevector2.global.config.VectorProperties
import com.sleekydz86.densevector2.global.error.VectorStudyException
import org.springframework.http.HttpStatus

internal fun vectorFailure(properties: VectorProperties, action: String, cause: Exception): VectorStudyException =
    VectorStudyException(
        HttpStatus.BAD_GATEWAY,
        "Elasticsearch $action 에 실패했습니다. url=${properties.elasticsearchUrl} index=${properties.indexName}",
        cause
    )

internal fun passageDocument(title: String, body: String, embedding: FloatArray): PassageDocument {
    val document = PassageDocument()
    document.title = title
    document.body = body
    document.embedding = embedding.toList()
    return document
}
