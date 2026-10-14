package com.sleekydz86.youtubedownload.global.embedding

import com.sleekydz86.youtubedownload.global.error.DownloadException
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sqrt

class HashingEmbedder(private val dims: Int) {
    fun embed(text: String): FloatArray {
        IndexOptionRules.requireDims(dims)
        val features = featuresOf(text)
        if (features.isEmpty()) throw DownloadException("임베딩할 문장이 비어 있습니다.")
        val buckets = FloatArray(dims)
        features.forEach { feature -> addFeature(buckets, feature) }
        return l2Normalize(buckets)
    }

    private fun featuresOf(text: String): List<String> {
        val compact = text.lowercase(Locale.KOREAN).replace(Regex("[^\\p{L}\\p{N}]+"), " ").trim()
        if (compact.isEmpty()) return emptyList()
        val words = compact.split(Regex("\\s+"))
        val grams = compact.replace(" ", "").windowed(size = 2, step = 1, partialWindows = false)
        return words + grams
    }

    private fun addFeature(buckets: FloatArray, feature: String) {
        val hash = feature.hashCode()
        val index = Math.floorMod(hash, buckets.size)
        val sign = if ((hash ushr 16) and 1 == 0) 1f else -1f
        buckets[index] += sign
    }

    private fun l2Normalize(buckets: FloatArray): FloatArray {
        val norm = sqrt(buckets.sumOf { value -> (value * value).toDouble() }).toFloat()
        if (abs(norm) < 1e-12f) throw DownloadException("임베딩 벡터 길이가 0입니다.")
        return FloatArray(buckets.size) { index -> buckets[index] / norm }
    }
}
