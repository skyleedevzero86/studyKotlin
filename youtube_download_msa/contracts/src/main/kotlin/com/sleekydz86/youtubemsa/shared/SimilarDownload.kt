package com.sleekydz86.youtubemsa.shared

data class SimilarDownload(
    val item: ListedDownload,
    val esScore: Double,
    val cosine: Double,
)
