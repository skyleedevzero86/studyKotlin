package com.sleekydz86.densevector2.global.elasticsearch

data class IndexDescription(
    val index: String,
    val dims: Int,
    val similarity: String,
    val indexType: String,
    val m: Int,
    val efConstruction: Int,
    val clusterVersion: String
)
