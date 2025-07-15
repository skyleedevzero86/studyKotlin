package com.kominioai.global.exception

import com.kominioai.domain.survey.domain.model.ParticipationId
import com.kominioai.domain.survey.domain.model.QuestionId
import com.kominioai.global.exception.base.BaseException
import com.kominioai.global.exception.base.ErrorCode
import com.kominioai.global.exception.base.ErrorSeverity
import com.kominioai.global.exception.base.ErrorType

sealed class QuizParticipationException(
    errorCode: ErrorCode,
    message: String? = null,
    cause: Throwable? = null
) : BaseException(errorCode, message, cause) {

    class ParticipationNotAllowed(message: String) : QuizParticipationException(
        errorCode = ErrorCode.QUIZ_PARTICIPATION_NOT_ALLOWED,
        message = message
    )

    class TimeExpired : QuizParticipationException(
        errorCode = ErrorCode.QUIZ_PARTICIPATION_TIME_EXPIRED,
        message = "퀴즈 참여 시간이 만료되었습니다."
    )

    class QuestionNotFound(questionId: String) : QuizParticipationException(
        errorCode = ErrorCode.QUIZ_QUESTION_NOT_FOUND,
        message = "질문을 찾을 수 없습니다: $questionId"
    )

    class InvalidAnswer(message: String) : QuizParticipationException(
        errorCode = ErrorCode.QUIZ_INVALID_ANSWER,
        message = message
    )

    class RequiredAnswerMissing(message: String) : QuizParticipationException(
        errorCode = ErrorCode.QUIZ_REQUIRED_ANSWER_MISSING,
        message = message
    )

    class InvalidQuestionType(message: String) : QuizParticipationException(
        errorCode = ErrorCode.QUIZ_INVALID_QUESTION_TYPE,
        message = message
    )

    class ParticipationNotFound(participationId: ParticipationId) : QuizParticipationException(
        errorCode = ErrorCode.QUIZ_PARTICIPATION_NOT_FOUND,
        message = "퀴즈 참여를 찾을 수 없습니다: ${participationId.value}"
    )

    class DuplicateParticipation(surveyId: String, phone: String) : QuizParticipationException(
        errorCode = ErrorCode.QUIZ_DUPLICATE_PARTICIPATION,
        message = "이미 참여한 퀴즈입니다: surveyId=$surveyId, phone=$phone"
    )

    class SurveyNotActive(surveyId: String) : QuizParticipationException(
        errorCode = ErrorCode.QUIZ_SURVEY_NOT_ACTIVE,
        message = "활성화되지 않은 퀴즈입니다: $surveyId"
    )
}