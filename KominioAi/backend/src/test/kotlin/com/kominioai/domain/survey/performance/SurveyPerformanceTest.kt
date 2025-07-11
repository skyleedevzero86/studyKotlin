package com.kominioai.domain.survey.performance

import com.kominioai.domain.survey.domain.model.domain.Answer
import com.kominioai.domain.survey.domain.model.domain.Survey
import com.kominioai.domain.survey.domain.model.domain.SurveyResponse
import com.kominioai.domain.survey.domain.valueobject.ResponseId
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.domain.valueobject.QuestionId
import com.kominioai.domain.survey.domain.valueobject.QuestionType
import com.kominioai.domain.survey.domain.model.SurveySettings
import io.kotest.matchers.longs.shouldBeLessThan
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.time.LocalDateTime

@TestMethodOrder(OrderAnnotation::class)
class SurveyPerformanceTest {

    @Test
    @Order(1)
    fun `should measure survey creation performance`() = runTest {
        val startTime = System.currentTimeMillis()

        repeat(1000) { i ->
            val _survey = Survey.create(
                title = "Performance Test Survey $i",
                description = "Performance test description",
                createdBy = UserId.from("testuser"),
                settings = SurveySettings()
            )

            delay(1)
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        println("Created 1000 surveys in ${duration}ms")
        println("Average time per survey: ${duration / 1000.0}ms")

        duration shouldBeLessThan 10000
    }

    @Test
    @Order(2)
    fun `should measure response submission performance`() = runTest {
        val surveyId = SurveyId.from("test-survey")
        val startTime = System.currentTimeMillis()

        repeat(5000) { i ->
            val answer = Answer.create(
                responseId = "response$i",
                questionId = QuestionId.from("question1"),
                questionType = QuestionType.TEXT,
                textAnswer = "Answer for response $i",
                selectedOptions = emptyList()
            )

            val _response = SurveyResponse.create(
                surveyId = surveyId,
                respondentId = UserId.from("respondent$i"),
                answers = listOf(answer),
                ipAddress = "127.0.0.1"
            )

            delay(1)
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        println("Created 5000 responses in ${duration}ms")
        println("Average time per response: ${duration / 5000.0}ms")

        duration shouldBeLessThan 15000
    }
}