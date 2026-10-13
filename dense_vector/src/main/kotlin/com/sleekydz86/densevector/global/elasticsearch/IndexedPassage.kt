package com.sleekydz86.densevector.global.elasticsearch

data class IndexedPassage(
    val id: Long,
    val title: String,
    val body: String,
    val embedding: FloatArray
)
