package com.kominioai.domain.reward.application.dto

import jakarta.validation.constraints.*

data class CalculateCreatorRewardRequest(
    @field:NotBlank(message = "설문 ID는 필수입니다")
    val surveyId: String,
    
    @field:NotBlank(message = "생성자 ID는 필수입니다")
    val creatorId: String,
    
    @field:Min(value = 0, message = "참여자 수는 0 이상이어야 합니다")
    val participationCount: Int,
    
    @field:Min(value = 1, message = "목표 참여자 수는 1 이상이어야 합니다")
    val targetCount: Int,
    
    @field:Min(value = 1, message = "기본 리워드 가치는 1 이상이어야 합니다")
    val baseRewardValue: Int = 1000
)
