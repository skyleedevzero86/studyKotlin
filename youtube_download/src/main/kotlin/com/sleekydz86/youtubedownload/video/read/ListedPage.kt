package com.sleekydz86.youtubedownload.video.read

data class ListedPage(
    val page: Int,
    val size: Int,
    val total: Long,
    val items: List<ListedDownload>,
)
