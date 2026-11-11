package com.sleekydz86.cdcmigration.attachment.infrastructure

import com.sleekydz86.cdcmigration.attachment.application.ObjectStorage
import com.sleekydz86.cdcmigration.attachment.domain.StorageLocation
import com.sleekydz86.cdcmigration.global.error.BusinessException
import com.sleekydz86.cdcmigration.global.error.ErrorCode
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.core.exception.SdkException
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.S3Exception
import java.util.concurrent.ConcurrentHashMap

@Component
class S3ObjectStorage(private val s3: S3Client) : ObjectStorage {

    private val knownBuckets = ConcurrentHashMap.newKeySet<String>()

    override fun put(location: StorageLocation, bytes: ByteArray, contentType: String?) {
        guard("저장", location) {
            ensureBucket(location.bucket)
            s3.putObject(
                { it.bucket(location.bucket).key(location.key).contentType(contentType ?: "application/octet-stream") },
                RequestBody.fromBytes(bytes),
            )
        }
    }

    override fun get(location: StorageLocation): ByteArray = guard("읽기", location) {
        s3.getObjectAsBytes { it.bucket(location.bucket).key(location.key) }.asByteArray()
    }

    private fun ensureBucket(bucket: String) {
        if (bucket in knownBuckets) {
            return
        }
        try {
            s3.headBucket { it.bucket(bucket) }
        } catch (exception: S3Exception) {
            if (exception.statusCode() != 404) {
                throw exception
            }
            s3.createBucket { it.bucket(bucket) }
        }
        knownBuckets += bucket
    }

    private fun <T> guard(action: String, location: StorageLocation, block: () -> T): T = try {
        block()
    } catch (exception: NoSuchKeyException) {
        throw BusinessException(ErrorCode.STORAGE_FAILED, "객체가 없습니다: $location", exception)
    } catch (exception: S3Exception) {
        val detail = exception.awsErrorDetails()?.errorMessage() ?: exception.message
        throw BusinessException(ErrorCode.STORAGE_FAILED, "$action 실패 (HTTP ${exception.statusCode()}) $location: $detail", exception)
    } catch (exception: SdkClientException) {
        throw BusinessException(ErrorCode.STORAGE_FAILED, "MinIO에 연결할 수 없습니다 ($location): ${exception.message}", exception)
    } catch (exception: SdkException) {
        throw BusinessException(ErrorCode.STORAGE_FAILED, "$action 실패 $location: ${exception.message}", exception)
    }
}
