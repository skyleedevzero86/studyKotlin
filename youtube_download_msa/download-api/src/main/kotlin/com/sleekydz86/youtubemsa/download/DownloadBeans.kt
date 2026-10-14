package com.sleekydz86.youtubemsa.download

import com.sleekydz86.youtubemsa.shared.HashingEmbedder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DownloadBeans {
    @Bean
    fun hashingEmbedder(properties: DownloadProperties): HashingEmbedder = HashingEmbedder(properties.dims)
}
