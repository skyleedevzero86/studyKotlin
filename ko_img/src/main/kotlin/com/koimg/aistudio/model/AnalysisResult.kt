package com.koimg.aistudio.model

data class AnalysisResult(
    val id: String,
    val analysisType: String,
    val imageUrl: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)