package com.kominioai.domain.survey.domain.valueobject

data class UserId(val value: String) {
    companion object {
        fun from(value: String): UserId = UserId(value)
    }
}