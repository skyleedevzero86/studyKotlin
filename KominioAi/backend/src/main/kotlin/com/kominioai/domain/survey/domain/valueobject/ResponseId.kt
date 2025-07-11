package com.kominioai.domain.survey.domain.valueobject

data class ResponseId(val value: String) {
    companion object {
        fun generate(): ResponseId = ResponseId(java.util.UUID.randomUUID().toString())
    }
}