package com.kominioai.global.validation.validator

import com.kominioai.global.validation.annotation.SafeText
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.util.regex.Pattern

class SafeTextValidator : ConstraintValidator<SafeText, String?> {
    
    private var maxLength: Int = 1000
    private var allowHtml: Boolean = false
    
    // XSS 공격 패턴들
    private val XSS_PATTERNS = listOf(
        Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL),
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("expression\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("eval\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("document\\.", Pattern.CASE_INSENSITIVE),
        Pattern.compile("window\\.", Pattern.CASE_INSENSITIVE),
        Pattern.compile("alert\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("confirm\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("prompt\\s*\\(", Pattern.CASE_INSENSITIVE)
    )
    
    // SQL Injection 패턴들
    private val SQL_INJECTION_PATTERNS = listOf(
        Pattern.compile("(?i)(union|select|insert|update|delete|drop|create|alter|exec|execute|script|eval|expression)"),
        Pattern.compile("(?i)(or\\s+1\\s*=\\s*1|or\\s+true|and\\s+1\\s*=\\s*1|and\\s+true)"),
        Pattern.compile("(?i)(--|#|/\\*|\\*/|xp_|sp_)"),
        Pattern.compile("(?i)(waitfor|delay|sleep|benchmark)")
    )
    
    // 특수 문자 및 이스케이프 시퀀스
    private val SPECIAL_CHAR_PATTERN = Pattern.compile("[<>\"'&]")
    
    override fun initialize(constraintAnnotation: SafeText) {
        this.maxLength = constraintAnnotation.maxLength
        this.allowHtml = constraintAnnotation.allowHtml
    }
    
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (value.isNullOrBlank()) {
            return true // null이나 빈 문자열은 허용 (필수 여부는 별도 검증)
        }
        
        // 길이 검증
        if (value.length > maxLength) {
            return false
        }
        
        // HTML 허용하지 않는 경우 특수 문자 검증
        if (!allowHtml && SPECIAL_CHAR_PATTERN.matcher(value).find()) {
            return false
        }
        
        // XSS 공격 패턴 검증
        if (XSS_PATTERNS.any { it.matcher(value).find() }) {
            return false
        }
        
        // SQL Injection 패턴 검증
        if (SQL_INJECTION_PATTERNS.any { it.matcher(value).find() }) {
            return false
        }
        
        // 제어 문자 검증 (null byte, backspace 등)
        if (value.any { it.code < 32 && it.code != 9 && it.code != 10 && it.code != 13 }) {
            return false
        }
        
        return true
    }
} 