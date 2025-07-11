package com.kominioai.global.util

import com.kominioai.domain.survey.presentation.rest.dto.common.SurveyDto
import com.kominioai.domain.survey.presentation.rest.dto.response.SurveyResponseDto
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.test.web.reactive.server.WebTestClient

object TestUtils {

    fun createMockUserDetails(username: String = "testuser"): UserDetails {
        return User.builder()
            .username(username)
            .password("password")
            .authorities("ROLE_USER")
            .build()
    }

    fun createMockJwtToken(username: String = "testuser"): String {
        return "mock-jwt-token-$username"
    }

    suspend fun WebTestClient.ResponseSpec.expectSurveyDto(): WebTestClient.BodySpec<SurveyDto, *> {
        return this.expectStatus().isOk
            .expectBody(SurveyDto::class.java)
    }

    suspend fun WebTestClient.ResponseSpec.expectSurveyResponseDto(): WebTestClient.BodySpec<SurveyResponseDto, *> {
        return this.expectStatus().isOk
            .expectBody(SurveyResponseDto::class.java)
    }
}