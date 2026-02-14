package com.sleekydz86.komfa.domain.user

class JoinRejectedException(val code: String, message: String) : IllegalArgumentException(message)
