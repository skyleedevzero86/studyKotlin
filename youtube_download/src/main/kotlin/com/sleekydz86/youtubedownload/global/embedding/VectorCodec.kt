package com.sleekydz86.youtubedownload.global.embedding

import com.sleekydz86.youtubedownload.global.error.DownloadException
import java.util.Locale

object VectorCodec {
    fun encode(vector: FloatArray): String =
        vector.joinToString(",") { value -> String.format(Locale.US, "%.8f", value) }

    fun decode(raw: String): FloatArray {
        if (raw.isBlank()) throw DownloadException("벡터 문자열이 비어 있습니다.")
        return raw.split(",").map { token -> parseToken(token) }.toFloatArray()
    }

    private fun parseToken(token: String): Float {
        val value = token.trim().toFloatOrNull()
        if (value == null || value.isNaN() || value.isInfinite()) {
            throw DownloadException("벡터 값을 읽을 수 없습니다. 값: $token")
        }
        return value
    }
}
