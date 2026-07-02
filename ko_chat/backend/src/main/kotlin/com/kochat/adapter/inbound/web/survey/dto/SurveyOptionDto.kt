package com.kochat.adapter.inbound.web.survey.dto

data class SurveyOptionDto(
    val id: Long,
    val optionNo: Int,
    val optionText: String,
    val selectCount: Long = 0,
)
