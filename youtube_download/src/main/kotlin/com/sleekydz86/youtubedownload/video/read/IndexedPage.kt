package com.sleekydz86.youtubedownload.video.read

data class IndexedPage(
    val page: Int,
    val size: Int,
    val total: Long,
    val items: List<IndexedDownload>,
)
