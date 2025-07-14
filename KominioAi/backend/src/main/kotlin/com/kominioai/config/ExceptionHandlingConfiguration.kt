package com.kominioai.config

import com.kominioai.global.exception.i18n.ErrorMessageResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.web.reactive.config.EnableWebFlux

@Configuration
@EnableWebFlux
class ExceptionHandlingConfiguration {

    @Bean
    fun messageSource(): ResourceBundleMessageSource {
        val messageSource = ResourceBundleMessageSource()
        messageSource.setBasenames("messages")
        messageSource.setDefaultEncoding("UTF-8")
        messageSource.setUseCodeAsDefaultMessage(false)
        return messageSource
    }

    @Bean
    fun errorMessageResolver(messageSource: ResourceBundleMessageSource): ErrorMessageResolver {
        return ErrorMessageResolver(messageSource)
    }
} 