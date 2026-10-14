package com.sleekydz86.youtubemsa.shared

object IndexOptionRules {
    private val indexTypes = setOf(
        "hnsw", "int8_hnsw", "int4_hnsw", "bbq_hnsw", "bbq_flat", "bbq_disk", "flat", "int8_flat", "int4_flat",
    )

    fun validate(dims: Int, indexType: String, similarity: String, m: Int, efConstruction: Int) {
        requireDims(dims)
        if (indexType !in indexTypes) {
            throw StudyException("index_options.type '$indexType'은 이 프로젝트에서 쓰지 않습니다.")
        }
        if (similarity !in setOf("cosine", "dot_product", "l2_norm", "max_inner_product")) {
            throw StudyException("similarity '$similarity'은 이 프로젝트에서 쓰지 않습니다.")
        }
        if (m !in 2..100) throw StudyException("HNSW m은 2 이상 100 이하여야 합니다. 입력값: $m")
        if (efConstruction !in 1..1000) {
            throw StudyException("HNSW ef_construction은 1 이상 1000 이하여야 합니다. 입력값: $efConstruction")
        }
    }

    fun requireDims(dims: Int) {
        if (dims !in 1..4096) throw StudyException("벡터 차원 수는 1 이상 4096 이하여야 합니다. 입력값: $dims")
    }

    fun usesHnsw(indexType: String): Boolean = indexType == "hnsw" || indexType.endsWith("_hnsw")
}
