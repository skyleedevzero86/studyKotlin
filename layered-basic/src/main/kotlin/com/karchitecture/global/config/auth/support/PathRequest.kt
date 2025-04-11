package com.karchitecture.global.config.auth.support

import org.springframework.util.PathMatcher

data class PathRequest(
    private val path: String,
    private val httpMethod: HttpMethod
) {
    fun matches(
        pathMatcher: PathMatcher,
        targetPath: String,
        pathMethod: String
    ): Boolean {
        return pathMatcher.match(path, targetPath) &&
                httpMethod.matches(pathMethod)
    }
}