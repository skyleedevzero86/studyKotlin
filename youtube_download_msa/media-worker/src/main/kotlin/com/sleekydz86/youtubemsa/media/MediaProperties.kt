package com.sleekydz86.youtubemsa.media

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "study")
data class MediaProperties(
    val downloadApi: String,
    val workDirectory: String,
    val ytdlp: YtDlp,
    val minio: Minio,
    val cdc: Cdc,
) {
    data class YtDlp(val command: String)
    data class Minio(val endpoint: String, val accessKey: String, val secretKey: String, val bucket: String)
    data class Cdc(val topic: String)
}
