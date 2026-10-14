package com.sleekydz86.youtubedownload.video

enum class DownloadStatus {
    REQUESTED,
    SAVING,
    SAVED,
    FAILED,
}

fun downloadStatus(raw: String): DownloadStatus? =
    DownloadStatus.entries.firstOrNull { it.name == raw }
