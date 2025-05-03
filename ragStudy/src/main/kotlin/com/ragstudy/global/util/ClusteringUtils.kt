package com.ragstudy.global.util

import kotlin.math.sqrt

object ClusteringUtils {

    fun calculateCosineSimilarity(vec1: List<Double>, vec2: List<Double>): Double {
        if (vec1.isEmpty() || vec2.isEmpty() || vec1.size != vec2.size) {
            return 0.0
        }

        val dotProduct = vec1.zip(vec2).sumOf { (a, b) -> a * b }
        val magnitude1 = sqrt(vec1.sumOf { it * it })
        val magnitude2 = sqrt(vec2.sumOf { it * it })

        return if (magnitude1 == 0.0 || magnitude2 == 0.0) 0.0
        else dotProduct / (magnitude1 * magnitude2)
    }

    fun euclideanDistance(vec1: List<Double>, vec2: List<Double>): Double {
        if (vec1.isEmpty() || vec2.isEmpty() || vec1.size != vec2.size) {
            return Double.MAX_VALUE
        }

        return sqrt(vec1.zip(vec2).sumOf { (a, b) -> (a - b) * (a - b) })
    }
}
