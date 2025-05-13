package com.functionstudy.ones.ch011.core

import com.functionstudy.ones.ch07.inter.OutcomeError

interface DomainError : OutcomeError {
    override val msg: String
}