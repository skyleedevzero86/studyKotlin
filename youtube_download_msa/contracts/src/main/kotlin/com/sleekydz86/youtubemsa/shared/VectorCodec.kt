package com.sleekydz86.youtubemsa.shared

import java.util.Locale

object VectorCodec {
    fun encode(vector: FloatArray): String =
        vector.joinToString(",") { value -> String.format(Locale.US, "%.8f", value) }

    fun decode(raw: String): FloatArray {
        if (raw.isBlank()) throw StudyException("벡터 문자열이 비어 있습니다.")
        return raw.split(",").map { token ->
            val value = token.trim().toFloatOrNull()
            if (value == null || value.isNaN() || value.isInfinite()) {
                throw StudyException("벡터 값을 읽을 수 없습니다. 값: $token")
            }
            value
        }.toFloatArray()
    }
}
