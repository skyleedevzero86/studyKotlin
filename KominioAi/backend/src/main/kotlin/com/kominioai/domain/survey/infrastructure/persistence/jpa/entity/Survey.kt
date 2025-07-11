package com.kominioai.domain.survey.infrastructure.persistence.jpa.entity

import com.kominioai.domain.survey.domain.model.Question
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "surveys")
data class Survey(
    @EmbeddedId
    val id: SurveyId,

    @Column(nullable = false)
    var title: String,

    @Column(length = 1000)
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    var status: SurveyStatus,

    @Embedded
    val createdBy: UserId,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @Column(name = "published_at")
    var publishedAt: Instant? = null,

    @Column(name = "closed_at")
    var closedAt: Instant? = null,

    @Column(name = "updated_at")
    var updatedAt: Instant? = null,

    @OneToMany(mappedBy = "survey", cascade = [CascadeType.ALL], orphanRemoval = true)
    val questions: MutableList<Question> = mutableListOf()
) {
    protected constructor() : this(
        id = SurveyId(""),
        title = "",
        description = null,
        status = SurveyStatus.DRAFT,
        createdBy = UserId(""),
        createdAt = Instant.now()
    )

    companion object {
        fun create(id: SurveyId, title: String, description: String?, createdBy: UserId): Survey {
            require(title.isNotBlank()) { "제목은 비어 있을 수 없습니다." }
            return Survey(
                id = id,
                title = title,
                description = description,
                status = SurveyStatus.DRAFT,
                createdBy = createdBy,
                createdAt = Instant.now()
            )
        }
    }

    fun publish(): Survey {
        require(status == SurveyStatus.DRAFT) { "초안 상태의 설문조사만 게시할 수 있습니다." }
        require(questions.isNotEmpty()) { "설문조사에는 최소 하나 이상의 질문이 있어야 합니다." }

        this.status = SurveyStatus.PUBLISHED
        this.publishedAt = Instant.now()
        this.updatedAt = Instant.now()
        return this
    }

    fun close(): Survey {
        require(status == SurveyStatus.PUBLISHED) { "게시된 설문조사만 마감할 수 있습니다." }

        this.status = SurveyStatus.CLOSED
        this.closedAt = Instant.now()
        this.updatedAt = Instant.now()
        return this
    }

    fun activate() {
        require(questions.isNotEmpty()) { "설문조사에는 최소 하나의 질문이 필요합니다." }
        this.status = SurveyStatus.ACTIVE
        this.updatedAt = Instant.now()
    }

    fun deactivate() {
        this.status = SurveyStatus.INACTIVE
        this.updatedAt = Instant.now()
    }

    fun addQuestion(question: Question) {
        require(status == SurveyStatus.DRAFT) { "초안 상태의 설문조사에만 질문을 추가할 수 있습니다." }
        require(!questions.any { it.id == question.id }) { "동일한 ID를 가진 질문이 이미 존재합니다." }

        questions.add(question)
        question.survey = this
        this.updatedAt = Instant.now()
    }

    fun removeQuestion(questionId: UUID) {
        require(status == SurveyStatus.DRAFT) { "초안 상태의 설문조사에서만 질문을 제거할 수 있습니다." }
        val questionToRemove = questions.find { it.id == questionId }
            ?: throw IllegalArgumentException("해당 ID의 질문을 찾을 수 없습니다: $questionId")

        questions.remove(questionToRemove)
        this.updatedAt = Instant.now()
    }

    fun updateTitle(newTitle: String) {
        require(status == SurveyStatus.DRAFT) { "초안 상태의 설문조사만 제목을 변경할 수 있습니다." }
        require(newTitle.isNotBlank()) { "제목은 비어 있을 수 없습니다." }
        this.title = newTitle
        this.updatedAt = Instant.now()
    }

    fun updateDescription(newDescription: String?) {
        require(status == SurveyStatus.DRAFT) { "초안 상태의 설문조사만 설명을 변경할 수 있습니다." }
        this.description = newDescription
        this.updatedAt = Instant.now()
    }
}