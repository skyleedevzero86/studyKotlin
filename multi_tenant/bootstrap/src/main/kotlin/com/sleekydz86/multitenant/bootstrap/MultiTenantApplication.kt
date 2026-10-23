package com.sleekydz86.multitenant.bootstrap

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.sleekydz86.multitenant"])
class MultiTenantApplication

fun main(args: Array<String>) {
    runApplication<MultiTenantApplication>(*args)
}
