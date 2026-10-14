package com.sleekydz86.youtubemsa.shared

data class SimilarPage(
    val dims: Int,
    val similarity: String,
    val k: Int,
    val items: List<SimilarDownload>,
)
