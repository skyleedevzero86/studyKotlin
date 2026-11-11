package com.sleekydz86.cdcmigration

import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
@MapperScan(basePackages = ["com.sleekydz86.cdcmigration"], annotationClass = org.apache.ibatis.annotations.Mapper::class)
class CdcMigrationApplication

fun main(args: Array<String>) {
    runApplication<CdcMigrationApplication>(*args)
}
