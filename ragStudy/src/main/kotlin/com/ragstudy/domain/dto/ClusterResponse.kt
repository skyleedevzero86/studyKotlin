package com.ragstudy.domain.dto

data class ClusterResponse(
    val clusters: List<List<String>>,
    val message: String,
    val status: Int
)
