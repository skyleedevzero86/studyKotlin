package com.sleekydz86.youtubedownload.video.read

data class SimilarDownload(
    val item: ListedDownload,
    val esScore: Double,
    val cosine: Double,
)
