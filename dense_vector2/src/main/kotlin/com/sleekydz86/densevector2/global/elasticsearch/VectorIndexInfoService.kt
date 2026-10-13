package com.sleekydz86.densevector2.global.elasticsearch

import com.sleekydz86.densevector2.global.seed.IndexInfoNotes
import org.springframework.stereotype.Service

@Service
class VectorIndexInfoService(private val vectorIndex: VectorIndexAdmin) {
    fun describe(): IndexInfoResponse {
        val described = vectorIndex.describe()
        return IndexInfoResponse(
            index = described.index,
            dims = described.dims,
            similarity = described.similarity,
            indexType = described.indexType,
            m = described.m,
            efConstruction = described.efConstruction,
            clusterVersion = described.clusterVersion,
            embedder = "hashing-bigram",
            note = IndexInfoNotes.text(described.clusterVersion)
        )
    }
}
