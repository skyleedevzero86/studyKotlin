package com.kobuckets.fileuploads.service

import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import com.kobuckets.config.MinioProperties
import io.minio.PutObjectArgs
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
class FileUploadService(
    private val minioClient: MinioClient,
    private val properties: MinioProperties
) {

    fun upload(file: MultipartFile): String {
        val bucket = properties.bucket

        // 버킷이 없다면 생성
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build())
        }

        val filename = UUID.randomUUID().toString() + "_" + file.originalFilename

        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucket)
                .`object`(filename)
                .stream(file.inputStream, file.size, -1)
                .contentType(file.contentType)
                .build()
        )

        return "${properties.url}/${bucket}/${filename}"
    }
}
