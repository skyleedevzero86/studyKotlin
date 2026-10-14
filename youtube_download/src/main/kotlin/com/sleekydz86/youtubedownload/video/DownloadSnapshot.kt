package com.sleekydz86.youtubedownload.video

import java.time.Instant

data class DownloadSnapshot(
    val id: Long,
    val videoId: VideoId,
    val title: String?,
    val status: DownloadStatus,
    val bucket: String?,
    val objectKey: String?,
    val fileName: String?,
    val sizeBytes: Long?,
    val errorMessage: String?,
    val updatedAt: Instant,
    val embeddingJson: String = "",
    val likeCount: Int = 0,
    val dislikeCount: Int = 0,
    val viewCount: Int = 0,
)
