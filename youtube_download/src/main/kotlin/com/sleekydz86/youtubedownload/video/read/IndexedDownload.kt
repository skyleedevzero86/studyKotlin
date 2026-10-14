package com.sleekydz86.youtubedownload.video.read

import com.sleekydz86.youtubedownload.video.DownloadStatus

data class IndexedDownload(
    val id: Long,
    val videoId: String,
    val title: String?,
    val status: DownloadStatus,
    val bucket: String?,
    val objectKey: String?,
    val fileName: String?,
    val sizeBytes: Long?,
    val errorMessage: String?,
    val updatedAt: String,
    val likeCount: Int = 0,
    val dislikeCount: Int = 0,
    val viewCount: Int = 0,
)
