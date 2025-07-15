package com.kominioai.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.kominioai.domain.survey.adapter.out.cache.QuizParticipationCacheEntity
import com.kominioai.domain.survey.adapter.out.cache.SurveyCacheEntity
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer


@Configuration
class RedisConfig {
    
    private val logger = LoggerFactory.getLogger(RedisConfig::class.java)
    
    @PostConstruct
    fun init() {
        logger.info("RedisConfig 초기화 완료")
    }

    @Bean
    @Primary
    fun reactiveRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, SurveyCacheEntity> {
        logger.info("ReactiveRedisTemplate 빈 생성 시작")
        
        val keySerializer = StringRedisSerializer()

        val objectMapper = ObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        )

        val valueSerializer = Jackson2JsonRedisSerializer(SurveyCacheEntity::class.java)
        valueSerializer.setObjectMapper(objectMapper)

        val serializationContext = RedisSerializationContext
            .newSerializationContext<String, SurveyCacheEntity>()
            .key(keySerializer)
            .value(valueSerializer)
            .hashKey(keySerializer)
            .hashValue(valueSerializer)
            .build()

        logger.info("ReactiveRedisTemplate 빈 생성 완료")
        return ReactiveRedisTemplate(factory, serializationContext)
    }

    @Bean
    fun reactiveRedisMessageListenerContainer(factory: ReactiveRedisConnectionFactory): ReactiveRedisMessageListenerContainer {
        logger.info("ReactiveRedisMessageListenerContainer 빈 생성")
        return ReactiveRedisMessageListenerContainer(factory)
    }

    @Bean
    fun redisMessageListener(): MessageListenerAdapter {
        logger.info("Redis MessageListener 빈 생성")
        return MessageListenerAdapter(RedisMessageHandler(), "handleMessage")
    }

    @Bean("surveyRedisTemplate")
    fun surveyRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, com.kominioai.domain.survey.domain.model.Survey> {
        logger.info("Survey RedisTemplate 빈 생성")
        
        val keySerializer = StringRedisSerializer()

        val objectMapper = ObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        )

        val valueSerializer = Jackson2JsonRedisSerializer(com.kominioai.domain.survey.domain.model.Survey::class.java)
        valueSerializer.setObjectMapper(objectMapper)

        val serializationContext = RedisSerializationContext
            .newSerializationContext<String, com.kominioai.domain.survey.domain.model.Survey>()
            .key(keySerializer)
            .value(valueSerializer)
            .hashKey(keySerializer)
            .hashValue(valueSerializer)
            .build()

        return ReactiveRedisTemplate(factory, serializationContext)
    }

    @Bean("quizParticipationRedisTemplate")
    fun quizParticipationRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, QuizParticipationCacheEntity> {
        logger.info("Quiz Participation RedisTemplate 빈 생성")

        val keySerializer = StringRedisSerializer()

        val objectMapper = ObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        )

        val valueSerializer = Jackson2JsonRedisSerializer(QuizParticipationCacheEntity::class.java)
        valueSerializer.setObjectMapper(objectMapper)

        val serializationContext = RedisSerializationContext
            .newSerializationContext<String, QuizParticipationCacheEntity>()
            .key(keySerializer)
            .value(valueSerializer)
            .hashKey(keySerializer)
            .hashValue(valueSerializer)
            .build()

        return ReactiveRedisTemplate(factory, serializationContext)
    }

}