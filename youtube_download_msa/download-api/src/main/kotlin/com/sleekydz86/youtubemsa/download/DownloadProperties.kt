package com.sleekydz86.youtubemsa.download

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "study")
data class DownloadProperties(
    val dims: Int,
    val cdc: Cdc,
) {
    data class Cdc(
        val connectUrl: String,
        val databaseHost: String,
        val databasePort: Int,
        val databaseUser: String,
        val databasePassword: String,
        val serverId: Long,
        val connector: String,
        val topicPrefix: String,
        val database: String,
    )
}
