package com.kominioai.global.config

import io.micrometer.core.aop.TimedAspect
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * Micrometer 메트릭 수집 설정
 * 
 * @author KominioAI Team
 * @since 1.0.0
 */
@Configuration
class MetricsConfiguration {

    /**
     * TimedAspect 설정 - @Timed 어노테이션 지원
     */
    @Bean
    fun timedAspect(meterRegistry: MeterRegistry): TimedAspect {
        return TimedAspect(meterRegistry)
    }

    /**
     * 메트릭 레지스트리 커스터마이저 - 공통 태그 설정
     */
    @Bean
    fun meterRegistryCustomizer(): MeterRegistryCustomizer<MeterRegistry> {
        return MeterRegistryCustomizer { registry ->
            registry.config()
                .commonTags("application", "kominioai-backend")
                .commonTags("version", "1.0.0")
        }
    }

    /**
     * 테스트 환경용 SimpleMeterRegistry
     */
    @Bean
    @Profile("test")
    fun simpleMeterRegistry(): SimpleMeterRegistry {
        return SimpleMeterRegistry()
    }
} 