package com.sleekydz86.oauth.global.aop

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RestController

@Aspect
@Component
class LoggingAspect {

    private val log = LoggerFactory.getLogger(javaClass)

    @Around("execution(* com.sleekydz86.oauth.domain..service..*(..))")
    fun logDomainCommand(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature.toShortString()
        log.info("[도메인-CUD] 시작: {}", signature)
        return try {
            val result = joinPoint.proceed()
            log.info("[도메인-CUD] 성공: {}", signature)
            result
        } catch (ex: Exception) {
            log.warn("[도메인-CUD] 실패: {} - {}", signature, ex.message)
            throw ex
        }
    }

    @Around("@within(restController)")
    fun logInboundAdapter(joinPoint: ProceedingJoinPoint, restController: RestController): Any? {
        val signature = joinPoint.signature.toShortString()
        log.info("[어댑터-IN] 시작: {}", signature)
        return try {
            val result = joinPoint.proceed()
            log.info("[어댑터-IN] 성공: {}", signature)
            result
        } catch (ex: Exception) {
            log.warn("[어댑터-IN] 실패: {} - {}", signature, ex.message)
            throw ex
        }
    }

    @Around("execution(* com.sleekydz86.oauth.global.application..*(..))")
    fun logGlobalApplication(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature.toShortString()
        log.info("[글로벌-조회] 시작: {}", signature)
        return try {
            val result = joinPoint.proceed()
            log.info("[글로벌-조회] 성공: {}", signature)
            result
        } catch (ex: Exception) {
            log.warn("[글로벌-조회] 실패: {} - {}", signature, ex.message)
            throw ex
        }
    }
}
