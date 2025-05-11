package com.functionstudy.ones.ch07.inter

interface OutcomeError

// OutcomeError를 구현하는 GenericError 클래스 정의
data class GenericOutcomeError(val throwable: Throwable) : OutcomeError