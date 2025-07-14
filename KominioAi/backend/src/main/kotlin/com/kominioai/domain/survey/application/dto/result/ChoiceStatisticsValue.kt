package com.kominioai.domain.survey.application.dto.result

import com.kominioai.domain.survey.domain.model.QuestionOptionId
import java.math.BigDecimal

data class ChoiceStatisticsValue(
    val optionId: QuestionOptionId,
    val content: String,
    val selectedCount: Int,
    val percentage: BigDecimal,
    val rank: Int
)