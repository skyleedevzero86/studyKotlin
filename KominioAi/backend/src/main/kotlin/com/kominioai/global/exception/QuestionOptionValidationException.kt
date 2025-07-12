package com.kominioai.global.exception

class QuestionOptionValidationException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}