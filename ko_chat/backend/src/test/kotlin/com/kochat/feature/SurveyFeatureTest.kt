package com.kochat.feature

import com.kochat.support.FeatureTestSupport
import com.kochat.support.TestLog
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DisplayName("설문조사 기능 테스트")
class SurveyFeatureTest : FeatureTestSupport() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @DisplayName("USER는 내 설문 목록 API에 접근할 수 있다")
    fun userCanAccessMySurveys() {
        registerActiveUser("my-survey-user", "pass1234!")
        val token = bearerToken("my-survey-user", "ROLE_USER")

        val result = mockMvc.get("/api/v1/surveys/my") {
            header("Authorization", token)
        }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    @DisplayName("USER 권한은 관리자 설문 API에 접근할 수 없다")
    fun userCannotAccessAdminSurveys() {
        registerActiveUser("survey-user", "pass1234!")
        val token = bearerToken("survey-user", "ROLE_USER")

        val result = mockMvc.get("/api/v1/admin/surveys") {
            header("Authorization", token)
        }.andReturn()

        assertEquals(403, result.response.status)
    }

    @Test
    @DisplayName("ADMIN은 설문 목록을 조회할 수 있다")
    fun adminCanListSurveys() {
        val admin = registerAdmin("survey-admin", "admin1234!@#")
        val token = bearerToken(admin.username, "ROLE_ADMIN")

        val result = mockMvc.get("/api/v1/admin/surveys") {
            header("Authorization", token)
        }.andReturn()

        assertEquals(200, result.response.status)
        assertTrue(result.response.contentAsString.contains("content"))
    }

    @Test
    @DisplayName("방장은 채팅방 설문을 생성하고 통계를 조회할 수 있다")
    fun roomOwnerCanCreateSurveyAndViewStatistics() {
        val name = "room-owner-survey-flow"
        TestLog.start(name)

        registerActiveUser("owner-user", "pass1234!")
        val ownerToken = bearerToken("owner-user", "ROLE_USER")

        val roomResult = mockMvc.post("/api/v1/chat-rooms") {
            header("Authorization", ownerToken)
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "name": "설문 테스트방",
                  "description": "survey test",
                  "type": "GROUP",
                  "maxMembers": 20,
                  "isPrivate": false,
                  "mediaMode": "TEXT"
                }
            """.trimIndent()
        }.andReturn()
        assertEquals(201, roomResult.response.status)
        val roomId = Regex(""""id"\s*:\s*(\d+)""").find(roomResult.response.contentAsString)?.groupValues?.get(1)
            ?: error("room id not found")

        val createResult = mockMvc.post("/api/v1/chat-rooms/$roomId/surveys") {
            header("Authorization", ownerToken)
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "title": "만족도 조사",
                  "description": "테스트 설문",
                  "targetMode": "ALL_MEMBERS",
                  "questions": [
                    {
                      "questionText": "만족하셨나요?",
                      "questionType": "SINGLE_CHOICE",
                      "options": [
                        { "optionText": "예" },
                        { "optionText": "아니오" }
                      ]
                    }
                  ]
                }
            """.trimIndent()
        }.andReturn()
        assertEquals(201, createResult.response.status)
        val surveyId = Regex(""""id"\s*:\s*(\d+)""").find(createResult.response.contentAsString)?.groupValues?.get(1)
            ?: error("survey id not found")

        val publishResult = mockMvc.post("/api/v1/chat-rooms/$roomId/surveys/$surveyId/publish") {
            header("Authorization", ownerToken)
        }.andReturn()
        assertEquals(200, publishResult.response.status)

        val statsResult = mockMvc.get("/api/v1/chat-rooms/$roomId/surveys/$surveyId/statistics") {
            header("Authorization", ownerToken)
        }.andReturn()
        assertEquals(200, statsResult.response.status)
        assertTrue(statsResult.response.contentAsString.contains("byQuestion"))

        val excelResult = mockMvc.get("/api/v1/chat-rooms/$roomId/surveys/$surveyId/statistics/export/excel") {
            header("Authorization", ownerToken)
        }.andReturn()
        assertEquals(200, excelResult.response.status)
        assertTrue(excelResult.response.contentAsByteArray.isNotEmpty())

        TestLog.end(name)
    }
}
