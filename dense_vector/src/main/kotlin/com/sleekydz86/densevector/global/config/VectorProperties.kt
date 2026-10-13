package com.sleekydz86.densevector.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "study.vector")
class VectorProperties {
    var elasticsearchUrl: String = "http://127.0.0.1:9200"
    var indexName: String = "dense-vector-passages"
    var dims: Int = 64
    var similarity: String = "cosine"
    var indexType: String = "hnsw"
    var m: Int = 16
    var efConstruction: Int = 100
    var numCandidates: Int = 100
    var startupSeed: Boolean = true
}
