package com.functionstudy.ones.ch011.core

import com.functionstudy.ones.ch07.inter.OutcomeError

sealed class DomainError(override val msg: String) : OutcomeError