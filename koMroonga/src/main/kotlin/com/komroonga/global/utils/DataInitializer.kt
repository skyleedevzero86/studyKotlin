package com.komroonga.global.utils

fun interface DataInitializer {
    fun initialize(): Result<Unit>
}