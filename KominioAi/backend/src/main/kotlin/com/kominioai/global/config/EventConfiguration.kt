package com.kominioai.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

/**
 * 이벤트 처리 관련 설정
 * 비동기 이벤트 처리, 스레드 풀 설정 등을 담당
 */
@Configuration
@EnableAsync
class EventConfiguration {
    
    /**
     * 이벤트 리스너 전용 스레드 풀 설정
     * 이벤트 처리를 위한 전용 스레드 풀을 제공하여 메인 스레드 블로킹 방지
     */
    @Bean("eventListenerTaskExecutor")
    fun eventListenerTaskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        
        // 코어 스레드 수: 기본 2개
        executor.setCorePoolSize(2)
        
        // 최대 스레드 수: 기본 10개
        executor.setMaxPoolSize(10)
        
        // 큐 용량: 기본 100개
        executor.setQueueCapacity(100)
        
        // 스레드 이름 접두사
        executor.setThreadNamePrefix("event-listener-")
        
        // 스레드 풀 종료 시 대기 시간 (초)
        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.setAwaitTerminationSeconds(30)
        
        // 스레드 풀 초기화
        executor.initialize()
        
        return executor
    }
    
    /**
     * 이벤트 발행 전용 스레드 풀 설정
     * 이벤트 발행을 위한 전용 스레드 풀을 제공
     */
    @Bean("eventPublisherTaskExecutor")
    fun eventPublisherTaskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        
        // 코어 스레드 수: 기본 1개
        executor.setCorePoolSize(1)
        
        // 최대 스레드 수: 기본 5개
        executor.setMaxPoolSize(5)
        
        // 큐 용량: 기본 50개
        executor.setQueueCapacity(50)
        
        // 스레드 이름 접두사
        executor.setThreadNamePrefix("event-publisher-")
        
        // 스레드 풀 종료 시 대기 시간 (초)
        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.setAwaitTerminationSeconds(30)
        
        // 스레드 풀 초기화
        executor.initialize()
        
        return executor
    }
    
    /**
     * 배치 이벤트 처리 전용 스레드 풀 설정
     * 배치 이벤트 처리를 위한 전용 스레드 풀을 제공
     */
    @Bean("batchEventTaskExecutor")
    fun batchEventTaskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        
        // 코어 스레드 수: 기본 1개
        executor.setCorePoolSize(1)
        
        // 최대 스레드 수: 기본 3개
        executor.setMaxPoolSize(3)
        
        // 큐 용량: 기본 20개
        executor.setQueueCapacity(20)
        
        // 스레드 이름 접두사
        executor.setThreadNamePrefix("batch-event-")
        
        // 스레드 풀 종료 시 대기 시간 (초)
        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.setAwaitTerminationSeconds(60)
        
        // 스레드 풀 초기화
        executor.initialize()
        
        return executor
    }
} 