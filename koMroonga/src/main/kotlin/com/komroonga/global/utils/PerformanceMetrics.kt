package com.komroonga.global.utils

/**
 * 성능 지표 데이터 클래스
 * 초기화 과정의 성능을 측정하고 보고서 생성
 */
data class PerformanceMetrics(
    val totalTimeMs: Long,
    val memberTimeMs: Long,
    val postTimeMs: Long,
    val beforeMemoryMB: Double,
    val afterMemoryMB: Double
) {
    companion object {
        private const val BASELINE_TOTAL_SECONDS = 2184.0 // 기준 총 시간(초)
        private const val BASELINE_POST_SECONDS = 1340.0 // 기준 게시글 생성 시간(초)
    }

    /**
     * 성능 보고서 생성
     */
    fun generateReport(): String {
        val totalTimeInSeconds = totalTimeMs / 1000.0
        val memberTimeInSeconds = memberTimeMs / 1000.0
        val postTimeInSeconds = postTimeMs / 1000.0

        val totalTimeImprovementPercent = if (BASELINE_TOTAL_SECONDS > 0) {
            ((BASELINE_TOTAL_SECONDS - totalTimeInSeconds) / BASELINE_TOTAL_SECONDS) * 100
        } else 0.0
        val postTimeImprovementPercent = if (BASELINE_POST_SECONDS > 0) {
            ((BASELINE_POST_SECONDS - postTimeInSeconds) / BASELINE_POST_SECONDS) * 100
        } else 0.0
        val memoryReduction = beforeMemoryMB - afterMemoryMB
        val memoryReductionPercent = if (beforeMemoryMB > 0) {
            (memoryReduction / beforeMemoryMB) * 100
        } else 0.0

        return """
            ==== 초기화 성능 지표 비교 ====
            총 초기화 시간: ${String.format("%.2f", totalTimeInSeconds)}초 (${String.format("%.2f", totalTimeInSeconds / 60.0)}분)
            - 향상률: ${String.format("%.1f", totalTimeImprovementPercent)}% 감소
            사용자 생성 시간: ${String.format("%.2f", memberTimeInSeconds)}초 (${String.format("%.2f", memberTimeInSeconds / 60.0)}분)
            게시글 생성 시간: ${String.format("%.2f", postTimeInSeconds)}초 (${String.format("%.2f", postTimeInSeconds / 60.0)}분)
            - 향상률: ${String.format("%.1f", postTimeImprovementPercent)}% 감소
            메모리 사용량: ${String.format("%.2f", beforeMemoryMB)}MB -> ${String.format("%.2f", afterMemoryMB)}MB
            - 향상률: ${String.format("%.1f", memoryReductionPercent)}% 감소
            ===============================
        """.trimIndent()
    }
}