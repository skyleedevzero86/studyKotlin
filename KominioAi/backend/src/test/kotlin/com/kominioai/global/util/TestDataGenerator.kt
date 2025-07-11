package com.kominioai.global.util

import com.kominioai.domain.survey.domain.model.Answer
import com.kominioai.domain.survey.domain.model.Question
import com.kominioai.domain.survey.domain.model.QuestionOption
import com.kominioai.domain.survey.infrastructure.persistence.jpa.entity.Survey
import com.kominioai.domain.survey.infrastructure.persistence.jpa.entity.SurveyResponse
import com.kominioai.domain.survey.domain.valueobject.QuestionType
import com.kominioai.domain.survey.domain.valueobject.ResponseId
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.global.util.data.BulkTestData
import java.time.Instant
import kotlin.random.Random

object TestDataGenerator {

    fun generateSurvey(
        id: SurveyId = SurveyId.generate(),
        title: String = "Test Survey ${Random.nextInt(1000)}",
        status: SurveyStatus = SurveyStatus.DRAFT,
        createdBy: UserId = UserId("testuser${Random.nextInt(100)}")
    ): Survey {
        return Survey(
            id = id,
            title = title,
            description = "Generated test survey description",
            status = status,
            createdBy = createdBy,
            createdAt = Instant.now()
        )
    }

    fun generateQuestion(
        surveyId: SurveyId,
        type: QuestionType = QuestionType.TEXT,
        isRequired: Boolean = false,
        orderIndex: Int = 1
    ): Question {
        val question = Question(
            surveyId = surveyId,
            title = "Test Question ${Random.nextInt(1000)}",
            type = type,
            isRequired = isRequired,
            orderIndex = orderIndex
        )

        if (type in listOf(QuestionType.SINGLE_CHOICE, QuestionType.MULTIPLE_CHOICE)) {
            repeat(Random.nextInt(3, 6)) { i ->
                question.addOption(
                    QuestionOption(
                        questionId = question.id,
                        text = "Option ${i + 1}",
                        orderIndex = i + 1
                    )
                )
            }
        }

        return question
    }

    fun generateSurveyResponse(
        surveyId: SurveyId,
        questionIds: List<String>,
        respondentId: UserId? = null
    ): SurveyResponse {
        val response = SurveyResponse(
            id = ResponseId.generate(),
            surveyId = surveyId,
            respondentId = respondentId,
            submittedAt = Instant.now()
        )

        questionIds.forEach { questionId ->
            response.addAnswer(
                Answer(
                    responseId = response.id,
                    questionId = questionId,
                    answerText = "Generated answer for question $questionId",
                    selectedOptionId = null
                )
            )
        }

        return response
    }

    fun generateBulkTestData(
        numberOfSurveys: Int = 10,
        questionsPerSurvey: Int = 5,
        responsesPerSurvey: Int = 20
    ): BulkTestData {
        val surveys = mutableListOf<Survey>()
        val responses = mutableListOf<SurveyResponse>()

        repeat(numberOfSurveys) { i ->
            val survey = generateSurvey(
                title = "Bulk Test Survey ${i + 1}",
                status = SurveyStatus.PUBLISHED
            )

            repeat(questionsPerSurvey) { j ->
                val question = generateQuestion(
                    surveyId = survey.id,
                    type = QuestionType.values()[Random.nextInt(QuestionType.values().size)],
                    orderIndex = j + 1
                )
                survey.addQuestion(question)
            }

            surveys.add(survey)

            repeat(responsesPerSurvey) { k ->
                val response = generateSurveyResponse(
                    surveyId = survey.id,
                    questionIds = survey.questions.map { it.id },
                    respondentId = UserId("respondent${k + 1}")
                )
                responses.add(response)
            }
        }

        return BulkTestData(surveys, responses)
    }
}