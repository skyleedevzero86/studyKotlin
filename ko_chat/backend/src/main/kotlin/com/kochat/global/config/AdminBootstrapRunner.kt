package com.kochat.global.config

import com.kochat.domain.user.service.UserCommandService
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class AdminBootstrapRunner(
    private val adminBootstrapProperties: AdminBootstrapProperties,
    private val userCommandService: UserCommandService,
) : CommandLineRunner {

    override fun run(vararg args: String) {
        if (!adminBootstrapProperties.enabled) {
            return
        }
        userCommandService.createBootstrapAdmin(
            username = adminBootstrapProperties.username,
            rawPassword = adminBootstrapProperties.password,
        )
    }
}
