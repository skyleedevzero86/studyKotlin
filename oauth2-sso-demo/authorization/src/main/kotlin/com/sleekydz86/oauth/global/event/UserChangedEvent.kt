package com.sleekydz86.oauth.global.event

import com.sleekydz86.oauth.domain.user.model.User

data class UserChangedEvent(
    val user: User,
)
