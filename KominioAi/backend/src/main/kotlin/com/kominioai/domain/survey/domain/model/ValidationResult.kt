package com.kominioai.domain.survey.domain.model

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Failure(val message: String) : ValidationResult()

    fun isSuccess(): Boolean = this is Success
    fun isFailure(): Boolean = this is Failure

    companion object {
        fun success(): ValidationResult = Success
        fun failure(message: String): ValidationResult = Failure(message)
    }
}