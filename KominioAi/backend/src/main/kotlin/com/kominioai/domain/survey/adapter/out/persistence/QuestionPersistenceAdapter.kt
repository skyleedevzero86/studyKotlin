package com.kominioai.domain.survey.adapter.out.persistence

import com.kominioai.domain.survey.application.port.out.QuestionPersistencePort
import com.kominioai.domain.survey.domain.model.Question
import com.kominioai.domain.survey.domain.model.QuestionId
import com.kominioai.domain.survey.domain.model.SurveyId
import io.r2dbc.spi.Row
import org.slf4j.LoggerFactory
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class QuestionPersistenceAdapter(
    private val client: DatabaseClient
) : QuestionPersistencePort {

    private val logger = LoggerFactory.getLogger(QuestionPersistenceAdapter::class.java)

    override fun findBySurveyId(surveyId: SurveyId): Flux<Question> {
        val sql = """
            SELECT q.*, qo.id as option_id, qo.content as option_content, qo.order as option_order
            FROM questions q
            LEFT JOIN question_options qo ON q.id = qo.question_id
            WHERE q.survey_id = :surveyId
            ORDER BY q.order, qo.order
        """.trimIndent()

        return client.sql(sql)
            .bind("surveyId", surveyId.value)
            .map { row, _ -> mapRowToQuestionWithOptions(row) }
            .all()
            .groupBy { it.first.id }
            .flatMap { group ->
                group.collectList()
                    .map { questionWithOptions ->
                        val question = questionWithOptions.first().first
                        val options = questionWithOptions.mapNotNull { it.second }
                        
                        // Options를 Question에 추가
                        options.forEach { option ->
                            question.addOption(option.getContent())
                        }
                        question
                    }
            }
            .doOnError { error ->
                logger.error("Questions 조회 실패: surveyId={}, error={}", surveyId.value, error.message)
            }
    }

    override fun findById(id: QuestionId): Mono<Question> {
        val sql = """
            SELECT q.*, qo.id as option_id, qo.content as option_content, qo.order as option_order
            FROM questions q
            LEFT JOIN question_options qo ON q.id = qo.question_id
            WHERE q.id = :id
            ORDER BY qo.order
        """.trimIndent()

        return client.sql(sql)
            .bind("id", id.value)
            .map { row, _ -> mapRowToQuestionWithOptions(row) }
            .all()
            .collectList()
            .map { questionWithOptions ->
                val question = questionWithOptions.first().first
                val options = questionWithOptions.mapNotNull { it.second }
                
                options.forEach { option ->
                    question.addOption(option.getContent())
                }
                question
            }
            .doOnError { error ->
                logger.error("Question 조회 실패: questionId={}, error={}", id.value, error.message)
            }
    }

    @Transactional
    override fun save(question: Question): Mono<QuestionId> {
        val questionEntity = QuestionEntity.fromDomain(question, "")
        val sql = """
            INSERT INTO questions (id, survey_id, content, type, order, is_required)
            VALUES (:id, :surveyId, :content, :type, :order, :isRequired)
        """.trimIndent()

        return client.sql(sql)
            .bind("id", questionEntity.id ?: "")
            .bind("surveyId", questionEntity.surveyId)
            .bind("content", questionEntity.content)
            .bind("type", questionEntity.type)
            .bind("order", questionEntity.order)
            .bind("isRequired", questionEntity.isRequired)
            .then()
            .thenReturn(question.id)
            .flatMap { questionId ->
                // Options 저장
                val options = question.getOptions()
                if (options.isNotEmpty()) {
                    saveQuestionOptions(options, questionId)
                        .thenReturn(questionId)
                } else {
                    Mono.just(questionId)
                }
            }
            .doOnSuccess { questionId ->
                logger.info("Question 저장 성공: questionId={}", questionId.value)
            }
            .doOnError { error ->
                logger.error("Question 저장 실패: question={}, error={}", question.id.value, error.message)
            }
    }

    @Transactional
    override fun update(question: Question): Mono<QuestionId> {
        val questionEntity = QuestionEntity.fromDomain(question, "")
        val sql = """
            UPDATE questions SET 
                content = :content, type = :type, order = :order, is_required = :isRequired
            WHERE id = :id
        """.trimIndent()

        return client.sql(sql)
            .bind("id", questionEntity.id ?: "")
            .bind("content", questionEntity.content)
            .bind("type", questionEntity.type)
            .bind("order", questionEntity.order)
            .bind("isRequired", questionEntity.isRequired)
            .then()
            .thenReturn(question.id)
            .flatMap { questionId ->
                deleteQuestionOptions(questionId)
                    .then(
                        Mono.defer {
                            val options = question.getOptions()
                            if (options.isNotEmpty()) {
                                saveQuestionOptions(options, questionId)
                            } else {
                                Mono.empty<Void>()
                            }
                        }
                    )
                    .thenReturn(questionId)
            }
            .doOnSuccess { questionId ->
                logger.info("Question 수정 성공: questionId={}", questionId.value)
            }
            .doOnError { error ->
                logger.error("Question 수정 실패: question={}, error={}", question.id.value, error.message)
            }
    }

    @Transactional
    override fun deleteById(id: QuestionId): Mono<Void> {
        return deleteQuestionOptions(id)
            .then(
                Mono.defer {
                    val sql = "DELETE FROM questions WHERE id = :id"
                    client.sql(sql)
                        .bind("id", id.value)
                        .then()
                }
            )
            .doOnSuccess {
                logger.info("Question 삭제 성공: questionId={}", id.value)
            }
            .doOnError { error ->
                logger.error("Question 삭제 실패: questionId={}, error={}", id.value, error.message)
            }
    }

    @Transactional
    override fun deleteBySurveyId(surveyId: SurveyId): Mono<Void> {

        val deleteOptionsSql = """
            DELETE FROM question_options 
            WHERE question_id IN (SELECT id FROM questions WHERE survey_id = :surveyId)
        """.trimIndent()
        
        return client.sql(deleteOptionsSql)
            .bind("surveyId", surveyId.value)
            .then()
            .then(
                Mono.defer {

                    val deleteQuestionsSql = "DELETE FROM questions WHERE survey_id = :surveyId"
                    client.sql(deleteQuestionsSql)
                        .bind("surveyId", surveyId.value)
                        .then()
                }
            )
            .doOnSuccess {
                logger.info("Survey의 모든 Questions 삭제 성공: surveyId={}", surveyId.value)
            }
            .doOnError { error ->
                logger.error("Survey의 모든 Questions 삭제 실패: surveyId={}, error={}", surveyId.value, error.message)
            }
    }

    override fun saveAll(questions: List<Question>): Flux<QuestionId> {
        return Flux.fromIterable(questions)
            .flatMap { question -> save(question) }
            .doOnError { error ->
                logger.error("Questions 일괄 저장 실패: error={}", error.message)
            }
    }

    fun saveAll(questions: List<Question>, surveyId: SurveyId): Flux<QuestionId> {
        return Flux.fromIterable(questions)
            .flatMap { question ->
                val questionEntity = QuestionEntity.fromDomain(question, surveyId.value)
                val sql = """
                    INSERT INTO questions (id, survey_id, content, type, order, is_required)
                    VALUES (:id, :surveyId, :content, :type, :order, :isRequired)
                """.trimIndent()

                client.sql(sql)
                    .bind("id", questionEntity.id ?: "")
                    .bind("surveyId", questionEntity.surveyId)
                    .bind("content", questionEntity.content)
                    .bind("type", questionEntity.type)
                    .bind("order", questionEntity.order)
                    .bind("isRequired", questionEntity.isRequired)
                    .then()
                    .thenReturn(question.id)
                    .flatMap { questionId ->
                        // Options 저장
                        val options = question.getOptions()
                        if (options.isNotEmpty()) {
                            saveQuestionOptions(options, questionId)
                                .thenReturn(questionId)
                        } else {
                            Mono.just(questionId)
                        }
                    }
            }
            .doOnError { error ->
                logger.error("Questions 일괄 저장 실패: surveyId={}, error={}", surveyId.value, error.message)
            }
    }

    private fun saveQuestionOptions(options: List<com.kominioai.domain.survey.domain.model.QuestionOption>, questionId: QuestionId): Mono<Void> {
        return Flux.fromIterable(options)
            .flatMap { option ->
                val optionEntity = QuestionOptionEntity.fromDomain(option, questionId.value)
                val sql = """
                    INSERT INTO question_options (id, question_id, content, order)
                    VALUES (:id, :questionId, :content, :order)
                """.trimIndent()

                client.sql(sql)
                    .bind("id", optionEntity.id ?: "")
                    .bind("questionId", optionEntity.questionId)
                    .bind("content", optionEntity.content)
                    .bind("order", optionEntity.order)
                    .then()
            }
            .then()
    }

    private fun deleteQuestionOptions(questionId: QuestionId): Mono<Void> {
        val sql = "DELETE FROM question_options WHERE question_id = :questionId"
        return client.sql(sql)
            .bind("questionId", questionId.value)
            .then()
    }

    private fun mapRowToQuestionWithOptions(row: Row): Pair<Question, com.kominioai.domain.survey.domain.model.QuestionOption?> {
        val question = Question.reconstruct(
            id = row.get("id", String::class.java) ?: "",
            content = row.get("content", String::class.java) ?: "",
            type = row.get("type", String::class.java) ?: "MULTIPLE_CHOICE",
            order = row.get("order", Number::class.java)?.toInt() ?: 1,
            isRequired = row.get("is_required", Boolean::class.java) ?: false,
            options = emptyList()
        )

        val optionId = row.get("option_id", String::class.java)
        val option = if (optionId != null) {
            com.kominioai.domain.survey.domain.model.QuestionOption.reconstruct(
                id = optionId,
                content = row.get("option_content", String::class.java) ?: "",
                order = row.get("option_order", Number::class.java)?.toInt() ?: 1
            )
        } else null

        return Pair(question, option)
    }
} 