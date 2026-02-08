package com.sleekydz86.skkk.domain.model

data class SearchResult(
    val id: String,
    val title: String,
    val url: String,
    val summary: String,
    val score: Double?
)
