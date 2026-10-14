package com.sleekydz86.youtubedownload.video

data class DownloadTicket(
    val id: Long,
    val videoId: VideoId,
    val status: DownloadStatus,
)
