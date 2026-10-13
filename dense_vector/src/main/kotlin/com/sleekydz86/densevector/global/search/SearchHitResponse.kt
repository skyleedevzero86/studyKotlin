package com.sleekydz86.densevector.global.search

data class SearchHitResponse(
    val id: Long,
    val title: String,
    val body: String,
    val esScore: Double,
    val cosine: Double
)
