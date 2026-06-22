package com.kochat.global.event

import com.kochat.domain.user.model.User

data class UserChangedEvent(
    val user: User,
)
