package com.sleekydz86.densevector2.global.embedding

import com.sleekydz86.densevector2.global.error.VectorStudyException
import org.springframework.http.HttpStatus

object IndexOptionRules {
    private val indexTypes = setOf(
        "hnsw",
        "int8_hnsw",
        "int4_hnsw",
        "bbq_hnsw",
        "bbq_flat",
        "bbq_disk",
        "flat",
        "int8_flat",
        "int4_flat"
    )
    private val similarities = setOf("cosine", "dot_product", "l2_norm", "max_inner_product")
    private val int4Types = setOf("int4_hnsw", "int4_flat")

    fun validate(dims: Int, indexType: String, similarity: String, m: Int, efConstruction: Int) {
        requireDims(dims)
        requireIndexType(indexType)
        requireEvenDimsForInt4(dims, indexType)
        requireSimilarity(similarity)
        requireHnswParameters(m, efConstruction)
    }

    fun requireDims(dims: Int) {
        if (dims !in 1..4096) {
            throw VectorStudyException(
                HttpStatus.BAD_REQUEST,
                "벡터 차원 수는 1 이상 4096 이하여야 합니다. 입력값: $dims"
            )
        }
    }

    fun usesHnsw(indexType: String): Boolean = indexType == "hnsw" || indexType.endsWith("_hnsw")

    private fun requireIndexType(indexType: String) {
        if (indexType in indexTypes) {
            return
        }
        throw VectorStudyException(
            HttpStatus.BAD_REQUEST,
            "index_options.type '$indexType'은 이 프로젝트에서 쓰지 않습니다. " +
                "가능한 값: ${indexTypes.joinToString(", ")}"
        )
    }

    private fun requireEvenDimsForInt4(dims: Int, indexType: String) {
        if (indexType in int4Types && dims % 2 != 0) {
            throw VectorStudyException(
                HttpStatus.BAD_REQUEST,
                "int4 양자화는 짝수 차원이 필요합니다. 입력값: $dims"
            )
        }
    }

    private fun requireSimilarity(similarity: String) {
        if (similarity in similarities) {
            return
        }
        throw VectorStudyException(
            HttpStatus.BAD_REQUEST,
            "similarity는 ${similarities.joinToString(", ")} 중 하나여야 합니다. 입력값: $similarity"
        )
    }

    private fun requireHnswParameters(m: Int, efConstruction: Int) {
        if (m !in 2..100) {
            throw VectorStudyException(HttpStatus.BAD_REQUEST, "HNSW m은 2 이상 100 이하여야 합니다. 입력값: $m")
        }
        if (efConstruction !in 1..1000) {
            throw VectorStudyException(
                HttpStatus.BAD_REQUEST,
                "HNSW ef_construction은 1 이상 1000 이하여야 합니다. 입력값: $efConstruction"
            )
        }
    }
}
