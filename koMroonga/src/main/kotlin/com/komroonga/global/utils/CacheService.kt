package com.komroonga.global.utils

import com.komroonga.global.error.model.AppError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.redis.RedisConnectionFailureException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.TimeoutException

@Component
class CacheService(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    // 캐시 활성화 여부 설정 (초기화 중에는 비활성화 가능)
    var enableCaching = true

    // 향상된 코루틴 디스패처 - IO 작업에 최적화
    private val redisCacheDispatcher = Dispatchers.IO.limitedParallelism(8)

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
                // 캐시 접근 실패 시 DB에서 직접 조회
                runCatching { compute() }
            }
        )
    }

    // 벌크 캐싱 메서드 추가
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