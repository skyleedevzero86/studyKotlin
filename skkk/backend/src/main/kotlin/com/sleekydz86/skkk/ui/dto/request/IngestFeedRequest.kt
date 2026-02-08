package com.sleekydz86.skkk.ui.dto.request

data class IngestFeedRequest(
    val feedUrl: String,
    val maxPosts: Int? = 50
)
