package com.sleekydz86.skkk.ui.dto.request

data class IngestListRequest(
    val listUrl: String,
    val maxItems: Int? = 50
)
