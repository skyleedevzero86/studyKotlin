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
    // 캐시에서 데이터를 가져오는 함수
    suspend fun <T> getFromCache(key: String): Result<T?> = runCatching {
        withContext(Dispatchers.IO) {
            try {
                @Suppress("UNCHECKED_CAST")
                redisTemplate.opsForValue().get(key) as? T
            } catch (e: RedisConnectionFailureException) {
                throw AppError.ExternalService(
                    message = "Redis 연결 실패",
                    cause = e,
                    serviceName = "Redis"
                )
            } catch (e: TimeoutException) {
                throw AppError.ExternalService(
                    message = "Redis 타임아웃",
                    cause = e,
                    serviceName = "Redis"
                )
            }
        }
    }

    // 캐시에 데이터를 저장하는 함수
    suspend fun <T> putInCache(key: String, value: T, ttl: Duration): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            try {
                redisTemplate.opsForValue().set(key, value as Any, ttl)
            } catch (e: RedisConnectionFailureException) {
                throw AppError.ExternalService(
                    message = "Redis 연결 실패",
                    cause = e,
                    serviceName = "Redis"
                )
            } catch (e: TimeoutException) {
                throw AppError.ExternalService(
                    message = "Redis 타임아웃",
                    cause = e,
                    serviceName = "Redis"
                )
            }
        }
    }

    // 캐시 또는 DB에서 데이터를 가져오는 함수
    suspend fun <T> getCachedOrCompute(
        key: String,
        ttl: Duration,
        compute: suspend () -> T
    ): Result<T> = getFromCache<T>(key).fold(
        onSuccess = { cachedValue ->
            if (cachedValue != null) {
                Result.success(cachedValue)
            } else {
                runCatching {
                    withContext(Dispatchers.IO) {
                        val computed = compute()
                        putInCache(key, computed, ttl)
                        computed
                    }
                }
            }
        },
        onFailure = { throwable ->
            Result.failure(throwable)
        }
    )
}