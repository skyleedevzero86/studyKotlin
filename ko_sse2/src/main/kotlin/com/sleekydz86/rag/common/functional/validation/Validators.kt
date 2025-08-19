package com.sleekydz86.rag.common.functional.validation

import com.sleekydz86.rag.common.functional.monad.Either

typealias ValidationResult<T> = Either<List<String>, T>

object Validators {
    fun <T> valid(value: T): ValidationResult<T> = Either.Right(value)
    fun <T> invalid(error: String): ValidationResult<T> = Either.Left(listOf(error))
    fun <T> invalid(errors: List<String>): ValidationResult<T> = Either.Left(errors)
}

fun String.validateNotBlank(field: String): ValidationResult<String> =
    if (this.isNotBlank()) Validators.valid(this)
    else Validators.invalid("$field 은(는) 비어있을 수 없습니다")

fun String.validateMaxLength(maxLength: Int, field: String): ValidationResult<String> =
    if (this.length <= maxLength) Validators.valid(this)
    else Validators.invalid("$field 의 길이는 $maxLength 자를 초과할 수 없습니다")

fun <T> ValidationResult<T>.and(other: ValidationResult<T>): ValidationResult<T> =
    when {
        this is Either.Left<List<String>> && other is Either.Left<List<String>> ->
            Either.Left(this.value + other.value)
        this is Either.Left<List<String>> -> this
        other is Either.Left<List<String>> -> other
        else -> this
    }