package com.sleekydz86.densevector2.global.elasticsearch

data class IndexedPassage(
    val id: Long,
    val title: String,
    val body: String,
    val embedding: FloatArray
)
