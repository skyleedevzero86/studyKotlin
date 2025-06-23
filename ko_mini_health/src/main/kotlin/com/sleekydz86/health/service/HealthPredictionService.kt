package com.sleekydz86.health.service

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.sleekydz86.health.entity.HealthLog
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.util.HashMap
import kotlin.math.roundToInt

@Service
class HealthPredictionService {

    private val environment = OrtEnvironment.getEnvironment()
    private var session: OrtSession? = null

    init {
        try {
            // ONNX 모델 로드
            val modelPath = ClassPathResource("models/health_risk_model.onnx").file
            session = environment.createSession(modelPath.absolutePath)
        } catch (e: Exception) {
            println("ONNX 모델 로드 실패: ${e.message}. 규칙 기반 로직으로 대체합니다.")
        }
    }

    fun predictRisk(healthLogs: List<HealthLog>): HashMap<String, Any> {
        val result = HashMap<String, Any>()
        if (healthLogs.isEmpty()) {
            result["riskLevel"] = "알 수 없음"
            result["message"] = "건강 데이터가 없습니다"
            return result
        }

        // 입력 데이터 준비
        val avgSleep = healthLogs.map { it.sleepHours }.average().toFloat()
        val avgSteps = healthLogs.map { it.steps }.average().toFloat()
        val avgStress = healthLogs.map { it.stressLevel }.average().toFloat()
        val avgHeartRate = healthLogs.map { it.heartRate }.average().toFloat()

        // ONNX 모델 사용 또는 규칙 기반 대체
        val riskScore = if (session != null) {
            try {
                // ONNX 모델 입력: [sleepHours, steps/1000, stressLevel, heartRate]
                val inputArray = floatArrayOf(avgSleep, avgSteps / 1000f, avgStress, avgHeartRate)
                val inputTensor = OnnxTensor.createTensor(environment, arrayOf(inputArray))
                val inputs = mapOf("input" to inputTensor)
                val outputs = session!!.run(inputs)
                val outputArray = (outputs[0].value as Array<FloatArray>)[0]

                // 출력 해석 (0=낮음, 1=보통, 2=높음)
                when (outputArray.maxOrNull()?.roundToInt()) {
                    0 -> "낮음"
                    1 -> "보통"
                    2 -> "높음"
                    else -> "알 수 없음"
                }
            } catch (e: Exception) {
                println("ONNX 예측 실패: ${e.message}. 규칙 기반 로직으로 대체합니다.")
                fallbackPredict(avgSleep, avgSteps, avgStress, avgHeartRate)
            }
        } else {
            fallbackPredict(avgSleep, avgSteps, avgStress, avgHeartRate)
        }

        result["riskLevel"] = riskScore
        result["message"] = "수면(${avgSleep}시간), 걸음수(${avgSteps}보), 스트레스(${avgStress}), 심박수(${avgHeartRate}bpm)를 기반으로 한 예측 위험도"
        return result
    }

    private fun fallbackPredict(avgSleep: Float, avgSteps: Float, avgStress: Float, avgHeartRate: Float): String {
        return when {
            avgSleep < 4 || avgStress > 8 || avgHeartRate > 100 || avgHeartRate < 60 -> "높음"
            avgSleep < 6 || avgStress > 6 || avgSteps < 5000 -> "보통"
            else -> "낮음"
        }
    }
}