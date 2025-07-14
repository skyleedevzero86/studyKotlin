package com.kominioai.global.exception.base

/**
 * 에러 타입 분류
 */
enum class ErrorType {
    DOMAIN,           // 도메인 비즈니스 로직 오류
    VALIDATION,       // 입력 데이터 검증 오류
    AUTHENTICATION,   // 인증 오류
    AUTHORIZATION,    // 인가 오류
    INFRASTRUCTURE,   // 인프라스트럭처 오류
    INTEGRATION,      // 외부 시스템 통합 오류
    SYSTEM           // 시스템 오류
} 