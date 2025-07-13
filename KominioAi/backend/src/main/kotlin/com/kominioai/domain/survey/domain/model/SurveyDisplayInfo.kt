package com.kominioai.domain.survey.domain.model

data class SurveyDisplayInfo(
    val statusMessage: String,
    val buttonInfo: ButtonInfo,
    val themeInfo: SurveyTheme,
    val participationInfo: ParticipationStatus,
    val requirementInfo: RequirementLevel
)