package com.sleekydz86.densevector2.global.search

data class SearchResponseBody(
    val dims: Int,
    val similarity: String,
    val k: Int,
    val hits: List<SearchHitResponse>
)
