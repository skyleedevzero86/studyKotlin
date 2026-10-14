package com.sleekydz86.youtubedownload.video.read

data class SimilarPage(
    val dims: Int,
    val similarity: String,
    val k: Int,
    val items: List<SimilarDownload>,
)
