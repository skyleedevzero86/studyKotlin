package com.kominioai.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class EventConfiguration {

    @Bean("eventListenerTaskExecutor")
    fun eventListenerTaskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()

        executor.setCorePoolSize(2)

        executor.setMaxPoolSize(10)

        executor.setQueueCapacity(100)

        executor.setThreadNamePrefix("event-listener-")

        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.setAwaitTerminationSeconds(30)

        executor.initialize()
        
        return executor
    }

    @Bean("eventPublisherTaskExecutor")
    fun eventPublisherTaskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()

        executor.setCorePoolSize(1)

        executor.setMaxPoolSize(5)

        executor.setQueueCapacity(50)

        executor.setThreadNamePrefix("event-publisher-")

        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.setAwaitTerminationSeconds(30)

        executor.initialize()
        
        return executor
    }

    @Bean("batchEventTaskExecutor")
    fun batchEventTaskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()

        executor.setCorePoolSize(1)

        executor.setMaxPoolSize(3)

        executor.setQueueCapacity(20)

        executor.setThreadNamePrefix("batch-event-")

        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.setAwaitTerminationSeconds(60)

        executor.initialize()
        
        return executor
    }
} 