package com.kominioai.global.validation.annotation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KClass

@Target(FIELD, PROPERTY, VALUE_PARAMETER)
@Retention(RUNTIME)
@Constraint(validatedBy = [UUIDValidator::class])
annotation class UUID(
    val message: String = "올바른 UUID 형식이 아닙니다",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)