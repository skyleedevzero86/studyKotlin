package com.sleekydz86.health.controller

import com.sleekydz86.health.dto.HealthLogDto
import com.sleekydz86.health.entity.HealthLog
import com.sleekydz86.health.entity.User
import com.sleekydz86.health.repository.HealthLogRepository
import com.sleekydz86.health.repository.UserRepository
import com.sleekydz86.health.service.AnomalyDetectionService
import com.sleekydz86.health.service.HealthPredictionService
import com.sleekydz86.health.service.HealthReportService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.HashMap

@RestController
class HealthController(
    private val userRepository: UserRepository,
    private val healthLogRepository: HealthLogRepository,
    private val predictionService: HealthPredictionService,
    private val reportService: HealthReportService,
    private val anomalyService: AnomalyDetectionService
) {

    @PostMapping("/health/user")
    fun saveUser(@RequestBody userDto: HashMap<String, String>): ResponseEntity<HashMap<String, Any>> {
        val userId = userDto["userId"] ?: return ResponseEntity.badRequest().body(
            HashMap<String, Any>().apply { put("message", "사용자 ID가 필요합니다") }
        )
        val userNm = userDto["userNm"] ?: return ResponseEntity.badRequest().body(
            HashMap<String, Any>().apply { put("message", "사용자 이름이 필요합니다") }
        )
        val height = userDto["height"]
        val weight = userDto["weight"]
        val gender = userDto["gender"]
        val bloodType = userDto["bloodType"]

        return try {
            val user = User(
                userId = userId,
                userNm = userNm,
                height = height,
                weight = weight,
                gender = gender,
                bloodType = bloodType
            )
            userRepository.save(user)
            ResponseEntity.ok(HashMap<String, Any>().apply { put("message", "사용자가 성공적으로 저장되었습니다") })
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(
                HashMap<String, Any>().apply { put("message", "사용자 저장 실패: ${e.message}") }
            )
        }
    }

    @PostMapping("/health/log")
    fun saveHealthLog(@RequestBody healthLogDto: HealthLogDto): ResponseEntity<HashMap<String, Any>> {
        val user = userRepository.findByUserId(healthLogDto.userId) ?: return ResponseEntity.badRequest().body(
            HashMap<String, Any>().apply { put("message", "사용자를 찾을 수 없습니다") }
        )

        val healthLog = HealthLog(
            userId = healthLogDto.userId,
            sleepHours = healthLogDto.sleepHours,
            steps = healthLogDto.steps,
            stressLevel = healthLogDto.stressLevel,
            heartRate = healthLogDto.heartRate,
            logDate = healthLogDto.logDate,
            memo = healthLogDto.memo // 메모 추가
        )

        val anomalyResult = anomalyService.detectAnomaly(healthLog)
        healthLog.warning = anomalyResult["warning"] as Boolean
        healthLogRepository.save(healthLog)

        return ResponseEntity.ok(anomalyResult)
    }

    @GetMapping("/health/report")
    fun getHealthReport(
        @RequestParam userId: String,
        @RequestParam period: String,
        @RequestParam(required = false) maxDays: Int?
    ): ResponseEntity<HashMap<String, Any>> {
        val user = userRepository.findByUserId(userId) ?: return ResponseEntity.badRequest().body(
            HashMap<String, Any>().apply { put("message", "사용자를 찾을 수 없습니다") }
        )

        val (startDate, endDate) = when (period.lowercase()) {
            "daily" -> Pair(LocalDateTime.now().minusDays(1), LocalDateTime.now())
            "weekly" -> Pair(LocalDateTime.now().minusDays(maxDays?.toLong() ?: 7), LocalDateTime.now())
            "monthly" -> Pair(LocalDateTime.now().minusMonths(1), LocalDateTime.now())
            else -> return ResponseEntity.badRequest().body(HashMap<String, Any>().apply { put("message", "잘못된 기간입니다") })
        }

        val healthLogs = healthLogRepository.findByUserIdAndDateRange(userId, startDate, endDate)
        if (healthLogs.isEmpty()) {
            return ResponseEntity.ok(HashMap<String, Any>().apply { put("message", "사용 가능한 데이터가 없습니다") })
        }

        val prediction = predictionService.predictRisk(healthLogs)
        val pdfContent = reportService.generateReport(user, healthLogs, period)

        val result = HashMap<String, Any>().apply {
            put("userNm", user.userNm)
            put("prediction", prediction)
            put("reportFileName", "${user.userNm}_${user.userId}_health_report_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}.pdf")
            put("reportContent", Base64.getEncoder().encodeToString(pdfContent))
            put("healthLogs", healthLogs.map { log ->
                HealthLogDto(
                    logId = log.logId,
                    userId = log.userId ?: "",
                    sleepHours = log.sleepHours,
                    steps = log.steps,
                    stressLevel = log.stressLevel,
                    heartRate = log.heartRate,
                    logDate = log.logDate,
                    warning = log.warning,
                    memo = log.memo
                )
            })
        }

        return ResponseEntity.ok(result)
    }
}