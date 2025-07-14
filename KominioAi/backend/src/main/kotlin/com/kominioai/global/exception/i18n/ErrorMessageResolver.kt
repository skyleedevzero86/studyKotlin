package com.kominioai.global.exception.i18n

import com.kominioai.global.exception.base.ErrorCode
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component
import java.util.Locale

/**
 * 다국어 에러 메시지 해석 컴포넌트
 */
@Component
class ErrorMessageResolver(
    private val messageSource: MessageSource
) {
    
    /**
     * 에러 코드에 따른 다국어 메시지 해석
     */
    fun resolveMessage(
        errorCode: ErrorCode,
        locale: Locale = LocaleContextHolder.getLocale(),
        params: Map<String, Any> = emptyMap()
    ): String {
        return try {
            val args = params.values.toTypedArray()
            messageSource.getMessage(
                errorCode.messageKey,
                args,
                errorCode.description, // 기본값으로 영어 설명 사용
                locale
            )
        } catch (e: Exception) {
            // 메시지 해석 실패 시 기본 메시지 반환
            errorCode.description
        }
    }
    
    /**
     * 필드별 검증 오류 메시지 해석
     */
    fun resolveFieldErrorMessage(
        field: String,
        errorCode: String,
        locale: Locale = LocaleContextHolder.getLocale(),
        params: Map<String, Any> = emptyMap()
    ): String {
        val messageKey = "validation.field.$field.$errorCode"
        return try {
            val args = params.values.toTypedArray()
            messageSource.getMessage(
                messageKey,
                args,
                "필드 '$field'에 오류가 있습니다",
                locale
            )
        } catch (e: Exception) {
            "필드 '$field'에 오류가 있습니다"
        }
    }
    
    /**
     * 사용자 친화적 에러 메시지 생성
     */
    fun createUserFriendlyMessage(
        errorCode: ErrorCode,
        locale: Locale = LocaleContextHolder.getLocale(),
        params: Map<String, Any> = emptyMap()
    ): String {
        return when (errorCode.severity) {
            com.kominioai.global.exception.base.ErrorSeverity.INFO -> 
                resolveMessage(errorCode, locale, params)
            com.kominioai.global.exception.base.ErrorSeverity.WARN -> 
                resolveMessage(errorCode, locale, params)
            com.kominioai.global.exception.base.ErrorSeverity.ERROR -> 
                resolveMessage(ErrorCode.UNEXPECTED_ERROR, locale, params)
            com.kominioai.global.exception.base.ErrorSeverity.CRITICAL -> 
                resolveMessage(ErrorCode.UNEXPECTED_ERROR, locale, params)
        }
    }
} 