package com.sleekydz86.densevector2.passage.list

import com.sleekydz86.densevector2.global.passage.PassageSummary

data class PassagePageResponse(
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val items: List<PassageSummary>
)
