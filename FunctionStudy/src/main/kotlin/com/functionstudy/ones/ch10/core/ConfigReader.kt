package com.functionstudy.ones.ch10.core

class ConfigReader<CTX, T>(val run: (CTX) -> T) {
    fun <R> map(f: (T) -> R): ConfigReader<CTX, R> =
        ConfigReader { ctx -> f(run(ctx)) }

    fun <R> flatMap(f: (T) -> ConfigReader<CTX, R>): ConfigReader<CTX, R> =
        ConfigReader { ctx -> f(run(ctx)).run(ctx) }
}
