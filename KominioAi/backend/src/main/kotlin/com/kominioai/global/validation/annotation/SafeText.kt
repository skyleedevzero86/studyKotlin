package com.kominioai.global.validation.annotation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KClass

@Target(FIELD, PROPERTY, VALUE_PARAMETER)
@Retention(RUNTIME)
@Constraint(validatedBy = [com.kominioai.global.validation.validator.SafeTextValidator::class])
annotation class SafeText(
    val message: String = "안전하지 않은 텍스트가 포함되어 있습니다",
    val maxLength: Int = 1000,
    val allowHtml: Boolean = false,
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
) 