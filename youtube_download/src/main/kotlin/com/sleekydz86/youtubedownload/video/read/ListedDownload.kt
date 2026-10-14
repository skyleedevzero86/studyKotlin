package com.sleekydz86.youtubedownload.video.read

data class ListedDownload(
    val id: Long,
    val videoId: String,
    val title: String?,
    val status: String,
    val fileName: String?,
    val sizeBytes: Long?,
    val errorMessage: String?,
    val updatedAt: String,
    val fileUrl: String?,
    val likeCount: Int = 0,
    val dislikeCount: Int = 0,
    val viewCount: Int = 0,
)
