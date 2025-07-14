package com.kominioai.global.exception.base

/**
 * 에러 심각도 레벨
 */
enum class ErrorSeverity {
    INFO,      // 정보성 메시지
    WARN,      // 경고 (사용자 조치 필요)
    ERROR,     // 오류 (시스템 조치 필요)
    CRITICAL   // 심각한 오류 (즉시 조치 필요)
} 