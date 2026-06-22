package com.kochat.support

object TestLog {

    fun start(displayName: String) {
        println()
        println("===== TEST START: $displayName =====")
    }

    fun end(displayName: String) {
        println("===== TEST END: $displayName =====")
        println()
    }

    fun given(scope: String, message: String) {
        println("[$scope] GIVEN: $message")
    }

    fun `when`(scope: String, message: String) {
        println("[$scope] WHEN: $message")
    }

    fun then(scope: String, message: String) {
        println("[$scope] THEN: $message")
    }
}
