package com.functionstudy.ones.ch09.core.result

class ConfigurationReader<T>(val runWith: (Map<String, String>) -> T) {
    fun <U> map(f: (T) -> U): ConfigurationReader<U> =
        ConfigurationReader { config -> f(runWith(config)) }

    fun <U> flatMap(f: (T) -> ConfigurationReader<U>): ConfigurationReader<U> =
        ConfigurationReader { config -> f(runWith(config)).runWith(config) }
}