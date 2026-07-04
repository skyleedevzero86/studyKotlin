package com.kochat.global.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.CommonErrorHandler
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff

@Configuration
@EnableKafka
@ConditionalOnProperty(prefix = "app.kafka", name = ["enabled"], havingValue = "true")
class KafkaConfig(
    private val kafkaProperties: KafkaProperties,
) {
    @Bean
    fun kafkaProducerFactory(): ProducerFactory<String, String> {
        val config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all",
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true,
            ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG to 15_000,
            ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG to 5_000,
            ProducerConfig.RETRY_BACKOFF_MS_CONFIG to 1_000,
            ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG to 1_000,
            ProducerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG to 30_000,
        )
        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, String>): KafkaTemplate<String, String> =
        KafkaTemplate(producerFactory)

    @Bean
    fun kafkaConsumerFactory(): ConsumerFactory<String, String> {
        val config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
            ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG to 1_000,
            ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG to 60_000,
            ConsumerConfig.RETRY_BACKOFF_MS_CONFIG to 1_000,
        )
        return DefaultKafkaConsumerFactory(config)
    }

    @Bean
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, String>,
        kafkaTemplate: KafkaTemplate<String, String>,
    ): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.setConsumerFactory(consumerFactory)
        factory.containerProperties.ackMode = ContainerProperties.AckMode.RECORD
        factory.setCommonErrorHandler(kafkaErrorHandler(kafkaTemplate))
        return factory
    }

    @Bean
    fun kafkaErrorHandler(kafkaTemplate: KafkaTemplate<String, String>): CommonErrorHandler {
        val recoverer = DeadLetterPublishingRecoverer(kafkaTemplate) { record, _ ->
            val dlqTopic = when (record.topic()) {
                kafkaProperties.topics.messageEvents,
                kafkaProperties.topics.messageEventsRetry,
                -> kafkaProperties.topics.messageEventsDlq

                kafkaProperties.topics.attachmentEvents,
                kafkaProperties.topics.attachmentEventsRetry,
                -> kafkaProperties.topics.attachmentEventsDlq

                else -> "${record.topic()}.dlq"
            }
            TopicPartition(dlqTopic, record.partition())
        }
        val maxRetries = (kafkaProperties.consumerRetryAttempts - 1).coerceAtLeast(0).toLong()
        return DefaultErrorHandler(recoverer, FixedBackOff(kafkaProperties.consumerRetryDelayMs, maxRetries))
    }
}
