package com.sleekydz86.youtubemsa.shared

data class ListedPage(
    val page: Int,
    val size: Int,
    val total: Long,
    val items: List<ListedDownload>,
)
