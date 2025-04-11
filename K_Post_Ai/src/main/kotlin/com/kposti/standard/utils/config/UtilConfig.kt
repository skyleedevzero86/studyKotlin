package com.kposti.standard.utils.config

import org.apache.tika.Tika
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UtilConfig {
    @Bean
    fun tika(): Tika {
        return Tika()
    }
}