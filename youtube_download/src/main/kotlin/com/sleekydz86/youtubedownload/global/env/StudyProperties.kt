package com.sleekydz86.youtubedownload.global.env

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "study")
data class StudyProperties(
    val elasticsearch: Elasticsearch,
    val minio: Minio,
    val ytdlp: YtDlp,
    val workDirectory: String,
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

    data class Minio(
        val endpoint: String,
        val accessKey: String,
        val secretKey: String,
        val bucket: String,
    )

    data class YtDlp(val command: String)

    data class Cdc(
        val topic: String,
        val connectUrl: String,
        val databaseHost: String,
        val databasePort: Int,
        val databaseUser: String,
        val databasePassword: String,
        val serverId: Long,
    )
}
