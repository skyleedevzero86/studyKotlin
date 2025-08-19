package com.sleekydz86.rag.common.functional.validation

import com.sleekydz86.rag.common.functional.monad.Either
import com.sleekydz86.rag.domain.model.ChatEntity
import org.springframework.stereotype.Component

@Component
class ChatEntityValidator {

    fun validate(chatEntity: ChatEntity): ValidationResult<ChatEntity> {
        val userNameValidation = chatEntity.currentUserName
            .validateNotBlank("사용자명")
            .flatMap { it.validateMaxLength(50, "사용자명") }

        val messageValidation = chatEntity.message
            .validateNotBlank("메시지")
            .flatMap { it.validateMaxLength(1000, "메시지") }

        return when {
            userNameValidation is Either.Left && messageValidation is Either.Left ->
                Either.Left(userNameValidation.value + messageValidation.value)
            userNameValidation is Either.Left -> userNameValidation
            messageValidation is Either.Left -> messageValidation
            else -> Either.Right(chatEntity)
        }
    }
}