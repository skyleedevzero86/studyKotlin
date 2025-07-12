package com.kominioai.global.validation.validator

import com.kominioai.global.validation.annotation.UUID
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.util.regex.Pattern

class UUIDValidator : ConstraintValidator<UUID, String> {
    
    private val UUID_PATTERN = Pattern.compile(
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    )
    
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (value.isNullOrBlank()) {
            return false
        }
        
        return UUID_PATTERN.matcher(value).matches()
    }
} 