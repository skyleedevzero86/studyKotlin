package com.sleekydz86.densevector2.support

import com.sleekydz86.densevector2.global.elasticsearch.IndexDescription
import com.sleekydz86.densevector2.global.elasticsearch.VectorIndexAdmin

class RecordingIndexAdmin : VectorIndexAdmin {
    var ensured = false

    override fun ensureIndex() {
        ensured = true
    }

    override fun describe(): IndexDescription = IndexDescription(
        index = "test-passages",
        dims = 64,
        similarity = "cosine",
        indexType = "hnsw",
        m = 16,
        efConstruction = 100,
        clusterVersion = "9.5.0"
    )
}
