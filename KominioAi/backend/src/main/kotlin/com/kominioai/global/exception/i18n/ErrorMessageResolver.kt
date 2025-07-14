package com.kominioai.global.exception.i18n

import com.kominioai.global.exception.base.ErrorCode
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.stereotype.Component
import java.util.*

@Component
class ErrorMessageResolver(
    private val messageSource: ResourceBundleMessageSource
) {
    fun resolveMessage(errorCode: ErrorCode, locale: Locale = Locale.getDefault()): String {
        return try {
            messageSource.getMessage(
                errorCode.messageKey,
                null,
                errorCode.description,
                locale
            ) ?: errorCode.description
        } catch (e: Exception) {
            errorCode.description
        }
    }
    
    fun resolveMessage(
        errorCode: ErrorCode, 
        args: Array<Any>, 
        locale: Locale = Locale.getDefault()
    ): String {
        return try {
            messageSource.getMessage(
                errorCode.messageKey,
                args,
                errorCode.description,
                locale
            ) ?: errorCode.description
        } catch (e: Exception) {
            errorCode.description
        }
    }

    fun resolveMessageWithFallback(
        errorCode: ErrorCode,
        fallbackMessage: String,
        locale: Locale = Locale.getDefault()
    ): String {
        return try {
            val message = messageSource.getMessage(
                errorCode.messageKey,
                null,
                fallbackMessage,
                locale
            )
            if (message == null || message == errorCode.messageKey) {
                fallbackMessage
            } else {
                message
            }
        } catch (e: Exception) {
            fallbackMessage
        }
    }
    
    fun resolveMessageWithLocale(
        errorCode: ErrorCode,
        locale: Locale? = null
    ): String {
        val targetLocale = locale ?: Locale.getDefault()
        return resolveMessage(errorCode, targetLocale)
    }
    fun hasMessage(errorCode: ErrorCode, locale: Locale = Locale.getDefault()): Boolean {
        return try {
            val message = messageSource.getMessage(
                errorCode.messageKey,
                null,
                null,
                locale
            )
            message != null && message != errorCode.messageKey
        } catch (e: Exception) {
            false
        }
    }
    fun getSupportedLocales(): List<Locale> {
        return listOf(
            Locale.KOREAN,
            Locale.ENGLISH,
            Locale.getDefault()
        )
    }
    fun resolveMessageForAllLocales(errorCode: ErrorCode): Map<Locale, String> {
        return getSupportedLocales().associateWith { locale ->
            resolveMessage(errorCode, locale)
        }
    }
    fun resolveMessageByKey(
        messageKey: String,
        defaultMessage: String,
        locale: Locale = Locale.getDefault()
    ): String {
        return try {
            messageSource.getMessage(
                messageKey,
                null,
                defaultMessage,
                locale
            ) ?: defaultMessage
        } catch (e: Exception) {
            defaultMessage
        }
    }
    fun resolveMessageByKey(
        messageKey: String,
        args: Array<Any>,
        defaultMessage: String,
        locale: Locale = Locale.getDefault()
    ): String {
        return try {
            messageSource.getMessage(
                messageKey,
                args,
                defaultMessage,
                locale
            ) ?: defaultMessage
        } catch (e: Exception) {
            defaultMessage
        }
    }
}