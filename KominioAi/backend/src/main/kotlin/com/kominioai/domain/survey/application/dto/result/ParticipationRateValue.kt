package com.kominioai.domain.survey.application.dto.result

import java.math.BigDecimal

data class ParticipationRateValue(
    val selectedCount: Int,
    val totalParticipants: Int,
    val percentage: BigDecimal
) {
    companion object {
        fun calculate(selected: Int, total: Int): ParticipationRateValue {
            require(selected >= 0 && total >= 0)
            val percent = if (total == 0) BigDecimal.ZERO
            else BigDecimal(selected * 100).divide(BigDecimal(total), 2, java.math.RoundingMode.HALF_UP)
            return ParticipationRateValue(selected, total, percent)
        }
    }
}