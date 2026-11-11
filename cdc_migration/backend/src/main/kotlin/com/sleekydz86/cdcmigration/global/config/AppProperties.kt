package com.sleekydz86.cdcmigration.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app")
data class AppProperties(
    val cors: List<String> = listOf("http://localhost:5175"),
    val attachment: Attachment = Attachment(),
    val storage: Storage = Storage(),
    val cdc: Cdc = Cdc(),
) {
    data class Attachment(
        val maxFileBytes: Long = 10L * 1024 * 1024,
        val bucket: String = "target-attachments",
    )

    data class Storage(
        val endpoint: String = "http://localhost:9100",
        val region: String = "us-east-1",
        val accessKey: String = "minioadmin",
        val secretKey: String = "minioadmin",
    )

    data class Cdc(
        val enabled: Boolean = true,
        val topicPattern: String = "legacy_r\\d+\\..*",
        val connectUrl: String = "http://localhost:8083",
        val pollIntervalMs: Long = 2000,
        val maxWaitingAttempts: Int = 30,
        val legacyDb: LegacyDb = LegacyDb(),
    )

    data class LegacyDb(
        val hostname: String = "legacy-db",
        val port: Int = 5432,
        val database: String = "legacy_db",
        val user: String = "legacy",
        val password: String = "legacy",
    )
}
