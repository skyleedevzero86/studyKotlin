package com.sleekydz86.health.service

import com.sleekydz86.health.entity.HealthLog
import org.springframework.stereotype.Service
import java.util.HashMap

@Service
class AnomalyDetectionService(
    private val sentimentAnalysisService: SentimentAnalysisService
) {

    fun detectAnomaly(healthLog: HealthLog): HashMap<String, Any> {
        val result = HashMap<String, Any>()
        var warning = false
        val messages = mutableListOf<String>()

        // 기존 규칙 기반 이상 징후 감지
        if (healthLog.heartRate > 100 || healthLog.heartRate < 60) {
            warning = true
            messages.add("비정상 심박수 감지: ${healthLog.heartRate} bpm")
        }
        if (healthLog.stressLevel > 8) {
            warning = true
            messages.add("높은 스트레스 수치 감지: ${healthLog.stressLevel}")
        }
        if (healthLog.sleepHours < 4) {
            warning = true
            messages.add("수면 부족 감지: ${healthLog.sleepHours}시간")
        }

        // 메모 감정 분석
        val sentiment = healthLog.memo?.let { sentimentAnalysisService.analyzeSentiment(it) }
        if (sentiment == "부정") {
            warning = true
            messages.add("부정적인 감정 상태 감지: ${healthLog.memo}")
        }

        if (warning) {
            println("사용자 ${healthLog.userId ?: "알 수 없음"}에 대한 이상 징후 감지: ${messages.joinToString(", ")}")
        }

        result["warning"] = warning
        result["messages"] = messages
        result["sentiment"] = sentiment ?: "없음"
        return result
    }
}