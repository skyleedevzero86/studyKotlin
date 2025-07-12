package com.kominioai.global.validation.annotation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.util.UUID as JavaUUID

class UUIDValidator : ConstraintValidator<UUID, String?> {

    override fun initialize(constraintAnnotation: UUID) {

    }

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {

        if (value == null) {
            return true
        }
        if (value.isBlank()) {
            return false
        }

        return try {
            JavaUUID.fromString(value)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}