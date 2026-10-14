package com.sleekydz86.youtubedownload.global.embedding

import com.sleekydz86.youtubedownload.global.error.DownloadException
import org.springframework.http.HttpStatus
import kotlin.math.sqrt

object CosineSimilarity {
    fun score(left: FloatArray, right: FloatArray): Double {
        if (left.size != right.size) {
            throw DownloadException("벡터 길이가 다릅니다. 왼쪽: ${left.size}, 오른쪽: ${right.size}")
        }
        val dot = left.indices.sumOf { index -> (left[index] * right[index]).toDouble() }
        val leftNorm = norm(left)
        val rightNorm = norm(right)
        if (leftNorm == 0.0 || rightNorm == 0.0) {
            throw DownloadException("길이가 0인 벡터는 코사인을 계산할 수 없습니다.", status = HttpStatus.BAD_REQUEST)
        }
        return dot / (leftNorm * rightNorm)
    }

    private fun norm(vector: FloatArray): Double =
        sqrt(vector.sumOf { value -> (value * value).toDouble() })
}
