package com.azure

import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@MapperScan("com.azure.global.mapper")
class KopringbootAzureOpenAiSdkApplication

fun main(args: Array<String>) {
    runApplication<KopringbootAzureOpenAiSdkApplication>(*args)
}
