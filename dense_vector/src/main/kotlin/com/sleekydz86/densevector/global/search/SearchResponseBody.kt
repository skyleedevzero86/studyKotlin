package com.sleekydz86.densevector.global.search

data class SearchResponseBody(
    val dims: Int,
    val similarity: String,
    val k: Int,
    val hits: List<SearchHitResponse>
)
