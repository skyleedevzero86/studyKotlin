package com.sleekydz86.skkk.ui.dto.response

data class IngestFeedResponse(
    val success: Boolean,
    val ingested: Int,
    val message: String
)
