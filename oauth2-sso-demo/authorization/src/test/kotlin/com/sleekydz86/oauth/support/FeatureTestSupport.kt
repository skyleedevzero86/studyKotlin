package com.sleekydz86.oauth.support

import com.sleekydz86.oauth.domain.user.model.ApproveUserCommand
import com.sleekydz86.oauth.domain.user.model.JoinCommand
import com.sleekydz86.oauth.domain.user.model.User
import com.sleekydz86.oauth.domain.user.model.UserRole
import com.sleekydz86.oauth.domain.user.service.UserCommandService
import com.sleekydz86.oauth.global.security.jwt.JwtTokenProvider
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
