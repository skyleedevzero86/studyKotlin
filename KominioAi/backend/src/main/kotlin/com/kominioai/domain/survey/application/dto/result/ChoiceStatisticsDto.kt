package com.kominioai.domain.survey.application.dto.result

import java.math.BigDecimal

data class ChoiceStatisticsDto(
    val optionId: String,
    val content: String,
    val selectedCount: Int,
    val percentage: BigDecimal,
    val rank: Int
)