package com.kobuckets.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "minio")
class MinioProperties {
    lateinit var url: String
    lateinit var accessKey: String
    lateinit var secretKey: String
    lateinit var bucket: String
}
