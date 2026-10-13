package com.sleekydz86.densevector2.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "study.vector")
class VectorProperties {
    var elasticsearchUrl: String = "http://127.0.0.1:9201"
    var indexName: String = "dense-vector2-passages"
    var dims: Int = 64
    var similarity: String = "cosine"
    var indexType: String = "bbq_hnsw"
    var m: Int = 16
    var efConstruction: Int = 100
    var numCandidates: Int = 100
    var startupSeed: Boolean = true
}
