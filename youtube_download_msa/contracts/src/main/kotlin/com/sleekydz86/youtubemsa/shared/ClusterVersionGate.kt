package com.sleekydz86.youtubemsa.shared

object ClusterVersionGate {
    fun assertLaterVectorStack(version: String) {
        val parts = version.split('.', '-', '+').mapNotNull { token -> token.toIntOrNull() }
        val major = parts.getOrElse(0) { 0 }
        val minor = parts.getOrElse(1) { 0 }
        if (major > 9 || (major == 9 && minor >= 5)) return
        throw StudyException(
            "bbq_hnsw, bbq_disk, vectordb_document는 Elasticsearch 9.5 이상 기능입니다. 연결된 클러스터=$version.",
            status = 409,
        )
    }
}
