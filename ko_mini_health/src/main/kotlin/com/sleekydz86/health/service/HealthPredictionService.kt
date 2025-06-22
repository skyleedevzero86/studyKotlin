package com.sleekydz86.health.service

import com.sleekydz86.health.entity.HealthLog
import org.springframework.stereotype.Service
import java.util.HashMap

@Service
class HealthPredictionService {

    fun predictRisk(healthLogs: List<HealthLog>): HashMap<String, Any> {
        val result = HashMap<String, Any>()
        if (healthLogs.isEmpty()) {
            result["riskLevel"] = "알 수 없음"
            result["message"] = "건강 데이터가 없습니다"
            return result
        }

        val avgSleep = healthLogs.map { it.sleepHours }.average()
        val avgSteps = healthLogs.map { it.steps }.average()
        val avgStress = healthLogs.map { it.stressLevel }.average()
        val avgHeartRate = healthLogs.map { it.heartRate }.average()

        val riskScore = when {
            avgSleep < 5.0 || avgStress > 8.0 || avgHeartRate > 100 -> "높음"
            avgSleep in 5.0..7.0 && avgStress in 5.0..8.0 -> "보통"
            else -> "낮음"
        }

        result["riskLevel"] = riskScore
        result["message"] = "수면(${avgSleep}시간), 걸음수(${avgSteps}보), 스트레스(${avgStress}), 심박수(${avgHeartRate}bpm)를 기반으로 한 예측 위험도"
        return result
    }
}