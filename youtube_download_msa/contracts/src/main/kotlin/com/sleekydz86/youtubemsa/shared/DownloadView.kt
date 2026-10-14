package com.sleekydz86.youtubemsa.shared

data class DownloadView(
    val id: Long = 0,
    val videoId: String = "",
    val watchUrl: String = "",
    val title: String? = null,
    val status: String = "",
    val bucket: String? = null,
    val objectKey: String? = null,
    val fileName: String? = null,
    val sizeBytes: Long? = null,
    val errorMessage: String? = null,
    val updatedAt: String = "",
    val embeddingJson: String = "",
    val likeCount: Int = 0,
    val dislikeCount: Int = 0,
    val viewCount: Int = 0,
)
