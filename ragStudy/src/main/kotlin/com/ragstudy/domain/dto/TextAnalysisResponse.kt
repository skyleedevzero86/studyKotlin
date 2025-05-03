package com.ragstudy.domain.dto

data class TextAnalysisResponse<T>(
    val data: T,
    val message: String,
    val status: Int
)