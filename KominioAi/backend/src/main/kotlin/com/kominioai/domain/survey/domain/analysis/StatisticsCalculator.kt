package com.kominioai.domain.survey.domain.analysis

import com.kominioai.domain.survey.application.dto.result.ChoiceStatisticsValue
import java.math.BigDecimal

object StatisticsCalculator {
    fun calculatePercentage(selected: Int, total: Int): BigDecimal =
        if (total == 0) BigDecimal.ZERO
        else BigDecimal(selected * 100).divide(BigDecimal(total), 2, java.math.RoundingMode.HALF_UP)

    fun assignRanks(statistics: List<ChoiceStatisticsValue>): List<ChoiceStatisticsValue> {
        val sorted = statistics.sortedWith(
            compareByDescending<ChoiceStatisticsValue> { it.selectedCount }
                .thenBy { it.optionId.value }
        )
        var lastCount: Int? = null
        var lastRank = 0
        var currentRank = 1
        return sorted.map {
            if (lastCount != null && it.selectedCount == lastCount) {
                lastRank
            } else {
                lastRank = currentRank
                lastCount = it.selectedCount
            }
            currentRank++
            it.copy(rank = lastRank)
        }
    }
}