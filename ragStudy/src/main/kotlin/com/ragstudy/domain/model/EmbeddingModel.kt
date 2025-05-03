package com.ragstudy.domain.model

interface EmbeddingModel {
    fun embed(text: String): FloatArray
}