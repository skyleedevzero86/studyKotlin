package com.kominioai.global.util

import com.kominioai.domain.survey.domain.model.domain.Answer
import com.kominioai.domain.survey.domain.model.domain.Question
import com.kominioai.domain.survey.domain.model.domain.QuestionOption
import com.kominioai.domain.survey.domain.model.domain.Survey
import com.kominioai.domain.survey.domain.model.domain.SurveyResponse
import com.kominioai.domain.survey.domain.valueobject.QuestionType
import com.kominioai.domain.survey.domain.valueobject.ResponseId
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.domain.valueobject.QuestionId
import com.kominioai.domain.survey.domain.model.SurveySettings
import com.kominioai.global.util.data.BulkTestData
import java.time.LocalDateTime
import kotlin.random.Random

object TestDataGenerator {

    fun generateSurvey(
        title: String = "Test Survey ${Random.nextInt(1000)}",
        createdBy: UserId = UserId.from("testuser${Random.nextInt(100)}")
    ): Survey {
        return Survey.create(
            title = title,
            description = "Generated test survey description",
            createdBy = createdBy,
            settings = SurveySettings()
        )
    }

    fun generateQuestion(
        surveyId: SurveyId,
        type: QuestionType = QuestionType.TEXT,
        isRequired: Boolean = false,
        orderIndex: Int = 1
    ): Question {
        val question = Question.create(
            surveyId = surveyId,
            order = orderIndex,
            text = "Test Question ${Random.nextInt(1000)}",
            description = null,
            type = type,
            required = isRequired,
            options = if (type in listOf(QuestionType.SINGLE_CHOICE, QuestionType.MULTIPLE_CHOICE)) {
                (1..Random.nextInt(3, 6)).map { "Option $it" }
            } else {
                emptyList()
            }
        )

        return question
    }

    fun generateSurveyResponse(
        surveyId: SurveyId,
        questionIds: List<QuestionId>,
        respondentId: UserId? = null
    ): SurveyResponse {
        val answers = questionIds.map { questionId ->
            Answer.create(
                responseId = "",
                questionId = questionId,
                questionType = QuestionType.TEXT,
                textAnswer = "Generated answer for question ${questionId.value}",
                selectedOptions = emptyList()
            )
        }

        return SurveyResponse.create(
            surveyId = surveyId,
            respondentId = respondentId,
            answers = answers,
            ipAddress = "127.0.0.1"
        )
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
                title = "Bulk Test Survey ${i + 1}"
            )

            val questions = mutableListOf<Question>()
            repeat(questionsPerSurvey) { j ->
                val question = generateQuestion(
                    surveyId = survey.id,
                    type = QuestionType.values()[Random.nextInt(QuestionType.values().size)],
                    orderIndex = j + 1
                )
                questions.add(question)
            }

            val surveyWithQuestions = questions.fold(survey) { acc, question ->
                acc.addQuestion(question)
            }
            surveys.add(surveyWithQuestions)

            repeat(responsesPerSurvey) { k ->
                val response = generateSurveyResponse(
                    surveyId = survey.id,
                    questionIds = questions.map { it.id },
                    respondentId = UserId.from("respondent${k + 1}")
                )
                responses.add(response)
            }
        }

        return BulkTestData(surveys, responses)
    }
}