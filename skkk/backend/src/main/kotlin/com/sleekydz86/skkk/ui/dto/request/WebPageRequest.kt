package com.sleekydz86.skkk.ui.dto.request

data class WebPageRequest(
    val id: String,
    val title: String,
    val content: String,
    val url: String,
    val publishedAt: String? = null,
    val summary: String? = null
)
