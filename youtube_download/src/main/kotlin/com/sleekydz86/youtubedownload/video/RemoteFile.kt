package com.sleekydz86.youtubedownload.video

data class RemoteFile(
    val bucket: String,
    val objectKey: String,
    val fileName: String,
    val sizeBytes: Long,
)
