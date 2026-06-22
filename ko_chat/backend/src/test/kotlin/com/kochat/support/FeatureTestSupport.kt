package com.kochat.support

import com.kochat.domain.user.model.ApproveUserCommand
import com.kochat.domain.user.model.JoinCommand
import com.kochat.domain.user.model.User
import com.kochat.domain.user.model.UserRole
import com.kochat.domain.user.service.UserCommandService
import com.kochat.global.security.jwt.JwtTokenProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
abstract class FeatureTestSupport {

    @Autowired
    protected lateinit var userCommandService: UserCommandService

    @Autowired
    protected lateinit var jwtTokenProvider: JwtTokenProvider

    protected fun registerPendingUser(username: String, password: String): User =
        userCommandService.join(JoinCommand(username, password))

    protected fun registerActiveUser(
        username: String,
        password: String,
        role: UserRole = UserRole.USER,
    ): User {
        registerPendingUser(username, password)
        return userCommandService.approve(ApproveUserCommand(username, role))
    }

    protected fun registerAdmin(username: String = "feature-admin", password: String = "admin1234!@#"): User =
        userCommandService.createBootstrapAdmin(username, password)

    protected fun bearerToken(username: String, role: String): String =
        "Bearer ${jwtTokenProvider.createAccessToken(username, role)}"
}
