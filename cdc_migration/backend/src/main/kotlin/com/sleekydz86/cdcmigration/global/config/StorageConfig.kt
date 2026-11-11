package com.sleekydz86.cdcmigration.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import java.net.URI

@Configuration
class StorageConfig {

    @Bean(destroyMethod = "close")
    fun s3Client(properties: AppProperties): S3Client {
        val storage = properties.storage
        return S3Client.builder()
            .endpointOverride(URI.create(storage.endpoint))
            .region(Region.of(storage.region))
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(storage.accessKey, storage.secretKey)))
            .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
            .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
            .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
            .build()
    }
}
