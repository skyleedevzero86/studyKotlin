package com.sleekydz86.densevector.global.passage

import com.sleekydz86.densevector.global.embedding.VectorCodec
import com.sleekydz86.densevector.global.error.VectorStudyException
import com.sleekydz86.densevector.global.persistence.PassageEntity
import org.springframework.http.HttpStatus

object PassageViews {
    fun summary(entity: PassageEntity) = PassageSummary(
        id = idOf(entity),
        title = entity.title,
        body = entity.body,
        dims = entity.dims,
        createdAt = entity.createdAt
    )

    fun detail(entity: PassageEntity) = PassageDetail(
        id = idOf(entity),
        title = entity.title,
        body = entity.body,
        dims = entity.dims,
        createdAt = entity.createdAt,
        embedding = VectorCodec.decode(entity.embeddingJson).toList()
    )

    fun newEntity(title: String, body: String, vector: FloatArray) = PassageEntity(
        title = title,
        body = body,
        dims = vector.size,
        embeddingJson = VectorCodec.encode(vector)
    )

    fun idOf(entity: PassageEntity): Long =
        entity.id ?: throw VectorStudyException(HttpStatus.INTERNAL_SERVER_ERROR, "저장 후 문단 id가 없습니다.")
}
