package com.sleekydz86.youtubemsa.search

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "study")
data class SearchProperties(
    val downloadApi: String,
    val elasticsearch: Elasticsearch,
    val minio: Minio,
    val cdc: Cdc,
) {
    data class Elasticsearch(
        val url: String,
        val index: String,
        val dims: Int,
        val similarity: String,
        val indexType: String,
        val m: Int,
        val efConstruction: Int,
        val numCandidates: Int,
    )

    data class Minio(val endpoint: String, val accessKey: String, val secretKey: String, val bucket: String)
    data class Cdc(val topic: String)
}
