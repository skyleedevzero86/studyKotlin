package com.kochat.adapter.outbound.storage

data class MinioStoredObject(
    val objectKey: String,
    val fileName: String,
    val mimeType: String,
    val size: Long,
    val url: String,
)
