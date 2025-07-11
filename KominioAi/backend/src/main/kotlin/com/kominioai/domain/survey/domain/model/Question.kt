package com.kominioai.domain.survey.domain.model

import com.kominioai.domain.survey.domain.valueobject.QuestionType
import com.kominioai.domain.survey.infrastructure.persistence.jpa.entity.Survey
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "questions")
data class Question(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val text: String,

    @Enumerated(EnumType.STRING)
    val type: QuestionType,

    @Column(name = "is_required", nullable = false)
    val isRequired: Boolean = false,

    @Column(name = "order_index", nullable = false)
    var orderIndex: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    var survey: Survey? = null,

    @OneToMany(mappedBy = "question", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val options: MutableList<QuestionOption> = mutableListOf()
) {
    protected constructor() : this(
        id = UUID.randomUUID(),
        text = "",
        type = QuestionType.SINGLE_CHOICE,
        isRequired = false,
        orderIndex = 0,
        survey = null
    )

    fun addOption(optionText: String, orderIndex: Int) {
        val option = QuestionOption(
            questionId = this.id.toString(),
            orderIndex = orderIndex,
            text = optionText,
            question = this
        )
        options.add(option)
    }

    fun addExistingOption(option: QuestionOption): QuestionOption {
        val newOption = option.copy(
            questionId = this.id.toString(),
            question = this
        )
        options.add(newOption)
        return newOption
    }
}