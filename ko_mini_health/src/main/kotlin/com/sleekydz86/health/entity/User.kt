package com.sleekydz86.health.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @Column(name = "user_id")
    var userId: String = "",
    var userNm: String = "",
    var height: String? = null,
    var weight: String? = null,
    var gender: String? = null,
    var bloodType: String? = null,
    var regDt: LocalDateTime = LocalDateTime.now()
)