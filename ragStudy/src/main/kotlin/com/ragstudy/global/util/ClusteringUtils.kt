package com.ragstudy.global.util

object ClusteringUtils {

    fun calculateCosineSimilarity(vec1: List<Double>, vec2: List<Double>): Double {
        val dotProduct = vec1.zip(vec2).sumOf { (a, b) -> a * b }
        val magnitude1 = Math.sqrt(vec1.sumOf { it * it })
        val magnitude2 = Math.sqrt(vec2.sumOf { it * it })

        return if (magnitude1 == 0.0 || magnitude2 == 0.0) 0.0
        else dotProduct / (magnitude1 * magnitude2)
    }
}
