package com.ragstudy.domain.entity

interface EmbeddingModel {
    fun embed(text: String): FloatArray
}
