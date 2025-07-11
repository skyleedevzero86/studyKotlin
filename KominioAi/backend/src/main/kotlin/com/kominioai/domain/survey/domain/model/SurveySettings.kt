package com.kominioai.domain.survey.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class SurveySettings(
    @Column(name = "allow_anonymous", nullable = false)
    val allowAnonymous: Boolean = true,

    @Column(name = "allow_multiple_responses", nullable = false)
    val allowMultipleResponses: Boolean = false,

    @Column(name = "require_login", nullable = false)
    val requireLogin: Boolean = false,

    @Column(name = "collect_ip_address", nullable = false)
    val collectIpAddress: Boolean = false
)