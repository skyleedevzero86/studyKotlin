package com.sleekydz86.densevector2.global.embedding

import com.sleekydz86.densevector2.global.error.VectorStudyException
import org.springframework.http.HttpStatus
import kotlin.math.sqrt

object CosineSimilarity {
    fun score(left: FloatArray, right: FloatArray): Double {
        requireSameLength(left, right)
        val dot = dot(left, right)
        val leftNorm = norm(left)
        val rightNorm = norm(right)
        if (leftNorm == 0.0 || rightNorm == 0.0) {
            throw VectorStudyException(HttpStatus.BAD_REQUEST, "길이가 0인 벡터는 코사인을 계산할 수 없습니다.")
        }
        return dot / (leftNorm * rightNorm)
    }

    private fun requireSameLength(left: FloatArray, right: FloatArray) {
        if (left.size == right.size) {
            return
        }
        throw VectorStudyException(
            HttpStatus.BAD_REQUEST,
            "벡터 길이가 다릅니다. 왼쪽: ${left.size}, 오른쪽: ${right.size}"
        )
    }

    private fun dot(left: FloatArray, right: FloatArray): Double =
        left.indices.sumOf { index -> (left[index] * right[index]).toDouble() }

    private fun norm(vector: FloatArray): Double =
        sqrt(vector.sumOf { value -> (value * value).toDouble() })
}
