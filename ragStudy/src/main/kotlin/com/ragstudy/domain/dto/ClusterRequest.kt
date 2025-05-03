package com.ragstudy.domain.dto

data class ClusterRequest(
    val documents: List<String>,
    val k: Int? = 3
)