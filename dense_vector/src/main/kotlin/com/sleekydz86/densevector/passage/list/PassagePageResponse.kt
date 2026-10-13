package com.sleekydz86.densevector.passage.list

import com.sleekydz86.densevector.global.passage.PassageSummary

data class PassagePageResponse(
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val items: List<PassageSummary>
)
