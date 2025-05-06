package com.komroonga.global.utils

import com.komroonga.global.error.model.AppError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.data.redis.RedisConnectionFailureException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.TimeoutException

/**
 * 캐시 서비스 클래스
 * Redis를 이용한 데이터 캐싱 기능 제공
 */
@Component
class CacheService(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    var enableCaching = true // 캐시 활성화 여부

    private val redisCacheDispatcher = Dispatchers.IO.limitedParallelism(16)

    /**
     * 캐시에 키가 존재하는지 확인
     */
    suspend fun exists(key: String): Boolean {
        if (!enableCaching) return false

        return runCatching {
            withContext(redisCacheDispatcher) {
                try {
                    redisTemplate.hasKey(key) ?: false
                } catch (e: Exception) {
                    logger.warn("캐시 키 확인 실패: ${e.message}")
                    false
                }
            }
        }.getOrDefault(false)
    }

    /**
     * 캐시에서 데이터 조회
     */
    suspend fun <T> getFromCache(key: String): Result<T?> {
        if (!enableCaching) return Result.success(null)

        return runCatching {
            withContext(redisCacheDispatcher) {
                try {
                    @Suppress("UNCHECKED_CAST")
                    redisTemplate.opsForValue().get(key) as? T
                } catch (e: RedisConnectionFailureException) {
                    throw AppError.ExternalService(
                        message = "Redis 연결 실패",
                        cause = e,
                        serviceName = "Redis",
                        errorCode = "REDIS_CONNECTION_FAILURE",
                        details = "Redis 서버에 연결할 수 없습니다."
                    )
                } catch (e: TimeoutException) {
                    throw AppError.ExternalService(
                        message = "Redis 타임아웃",
                        cause = e,
                        serviceName = "Redis",
                        errorCode = "REDIS_TIMEOUT",
                        details = "Redis 요청이 시간 초과되었습니다."
                    )
                }
            }
        }
    }

    /**
     * 캐시에 데이터 저장
     */
    suspend fun <T> putInCache(key: String, value: T, ttl: Duration): Result<Unit> {
        if (!enableCaching) return Result.success(Unit)

        return runCatching {
            withContext(redisCacheDispatcher) {
                try {
                    redisTemplate.opsForValue().set(key, value as Any, ttl)
                } catch (e: RedisConnectionFailureException) {
                    throw AppError.ExternalService(
                        message = "Redis 연결 실패",
                        cause = e,
                        serviceName = "Redis",
                        errorCode = "REDIS_CONNECTION_FAILURE",
                        details = "Redis 서버에 연결할 수 없습니다."
                    )
                } catch (e: TimeoutException) {
                    throw AppError.ExternalService(
                        message = "Redis 타임아웃",
                        cause = e,
                        serviceName = "Redis",
                        errorCode = "REDIS_TIMEOUT",
                        details = "Redis 요청이 시간 초과되었습니다."
                    )
                }
            }
        }
    }

    /**
     * 캐시된 데이터 조회 또는 새로 계산
     */
    suspend fun <T> getCachedOrCompute(
        key: String,
        ttl: Duration,
        compute: suspend () -> T
    ): Result<T> {
        if (!enableCaching) {
            return runCatching { compute() }
        }

        return getFromCache<T>(key).fold(
            onSuccess = { cachedValue ->
                if (cachedValue != null) {
                    Result.success(cachedValue)
                } else {
                    runCatching {
                        val computed = compute()
                        putInCache(key, computed, ttl)
                        computed
                    }
                }
            },
            onFailure = { throwable ->
                logger.warn("캐시 접근 실패: ${throwable.message}. 원본 데이터 조회 중...")
                runCatching { compute() }
            }
        )
    }

    /**
     * 벌크 데이터 캐싱
     */
    suspend fun <K, V> putBulkInCache(items: Map<K, V>, keyPrefix: String, ttl: Duration): Result<Unit> {
        if (!enableCaching || items.isEmpty()) return Result.success(Unit)

        return runCatching {
            withContext(redisCacheDispatcher) {
                val operations = redisTemplate.opsForValue()
                items.forEach { (key, value) ->
                    operations.set("$keyPrefix:$key", value as Any, ttl)
                }
            }
        }
    }
}