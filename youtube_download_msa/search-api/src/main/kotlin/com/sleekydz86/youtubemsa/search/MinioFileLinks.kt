package com.sleekydz86.youtubemsa.search

import io.minio.GetPresignedObjectUrlArgs
import io.minio.MinioClient
import io.minio.http.Method
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class MinioFileLinks(properties: SearchProperties) : FileLinks {
    private val client = MinioClient.builder()
        .endpoint(properties.minio.endpoint)
        .credentials(properties.minio.accessKey, properties.minio.secretKey)
        .build()

    override fun link(bucket: String, objectKey: String): String =
        client.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucket)
                .`object`(objectKey)
                .expiry(15, TimeUnit.MINUTES)
                .build(),
        )
}
