package com.sleekydz86.densevector.global.elasticsearch

data class IndexInfoResponse(
    val index: String,
    val dims: Int,
    val similarity: String,
    val indexType: String,
    val m: Int,
    val efConstruction: Int,
    val clusterVersion: String,
    val embedder: String,
    val note: String
)
