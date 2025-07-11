package com.kominioai.domain.survey.performance

import com.kominioai.domain.survey.domain.model.Answer
import com.kominioai.domain.survey.domain.model.Survey
import com.kominioai.domain.survey.domain.model.SurveyResponse
import com.kominioai.domain.survey.domain.valueobject.ResponseId
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import io.kotest.matchers.longs.shouldBeLessThan
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.time.Instant

@TestMethodOrder(OrderAnnotation::class)
class SurveyPerformanceTest {

    @Test
    @Order(1)
    fun `should measure survey creation performance`() = runTest {
        val startTime = System.currentTimeMillis()

        repeat(1000) { i ->
            val survey = Survey(
                id = SurveyId.generate(),
                title = "Performance Test Survey $i",
                description = "Performance test description",
                status = SurveyStatus.DRAFT,
                createdBy = UserId("testuser"),
                createdAt = Instant.now()
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
        val surveyId = SurveyId.generate()
        val startTime = System.currentTimeMillis()

        repeat(5000) { i ->
            val response = SurveyResponse(
                id = ResponseId.generate(),
                surveyId = surveyId,
                respondentId = UserId("respondent$i"),
                submittedAt = Instant.now()
            )

            repeat(5) { j ->
                response.addAnswer(
                    Answer(
                        responseId = response.id,
                        questionId = "question$j",
                        answerText = "Answer $j for response $i",
                        selectedOptionId = null
                    )
                )
            }

            delay(1)
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        println("Created 5000 responses in ${duration}ms")
        println("Average time per response: ${duration / 5000.0}ms")

        duration shouldBeLessThan 15000
    }
}