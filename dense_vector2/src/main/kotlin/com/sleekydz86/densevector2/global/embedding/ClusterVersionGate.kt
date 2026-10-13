package com.sleekydz86.densevector2.global.embedding

import com.sleekydz86.densevector2.global.error.VectorStudyException
import org.springframework.http.HttpStatus

object ClusterVersionGate {
    fun assertLaterVectorStack(version: String) {
        val parts = numericParts(version)
        val major = parts.getOrElse(0) { 0 }
        val minor = parts.getOrElse(1) { 0 }
        if (major > 9 || (major == 9 && minor >= 5)) {
            return
        }
        throw VectorStudyException(
            HttpStatus.CONFLICT,
            "bbq_hnsw, bbq_disk, vectordb_document는 Elasticsearch 9.5 이상 기능입니다. " +
                "연결된 클러스터=$version. 8.15 학습은 dense_vector 프로젝트를 사용하세요."
        )
    }

    private fun numericParts(version: String): List<Int> =
        version.split('.', '-', '+').mapNotNull { token -> token.toIntOrNull() }
}
