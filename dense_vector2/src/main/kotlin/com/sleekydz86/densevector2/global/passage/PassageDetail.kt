package com.sleekydz86.densevector2.global.passage

import java.time.LocalDateTime

data class PassageDetail(
    val id: Long,
    val title: String,
    val body: String,
    val dims: Int,
    val createdAt: LocalDateTime?,
    val embedding: List<Float>
)
