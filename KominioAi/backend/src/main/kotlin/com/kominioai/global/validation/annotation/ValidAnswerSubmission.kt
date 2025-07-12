package com.kominioai.global.validation.annotation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KClass

@Target(FIELD, PROPERTY, VALUE_PARAMETER)
@Retention(RUNTIME)
@Constraint(validatedBy = [com.kominioai.global.validation.validator.ValidAnswerSubmissionValidator::class])
annotation class ValidAnswerSubmission(
    val message: String = "답변 제출 데이터가 유효하지 않습니다",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
) 