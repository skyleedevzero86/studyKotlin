package com.sleekydz86.youtubemsa.media

import com.sleekydz86.youtubemsa.shared.DeliveryPolicy
import com.sleekydz86.youtubemsa.shared.deadLetterTopic
import org.apache.kafka.common.TopicPartition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.ExponentialBackOff

@Configuration
class MediaDeliveryConfig(
    private val consumerFactory: ConsumerFactory<String, String>,
    private val template: KafkaTemplate<String, String>,
) {
    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.setConsumerFactory(consumerFactory)
        factory.containerProperties.ackMode = ContainerProperties.AckMode.RECORD
        val backOff = ExponentialBackOff(DeliveryPolicy.initialDelayMillis, DeliveryPolicy.multiplier)
        backOff.maxInterval = DeliveryPolicy.maxDelayMillis
        backOff.maxAttempts = (DeliveryPolicy.maxAttempts - 1).toLong()
        val recoverer = DeadLetterPublishingRecoverer(template) { _, _ -> TopicPartition(deadLetterTopic, -1) }
        factory.setCommonErrorHandler(DefaultErrorHandler(recoverer, backOff))
        return factory
    }
}
