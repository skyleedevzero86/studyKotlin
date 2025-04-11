package com.karchitecture.global.config.auth.support

import org.springframework.util.AntPathMatcher
import org.springframework.util.PathMatcher

class PathContainer(
    private val pathMatcher: PathMatcher = AntPathMatcher(),
    private val includePatterns: MutableList<PathRequest> = mutableListOf(),
    private val excludePatterns: MutableList<PathRequest> = mutableListOf()
) {

    fun isNotIncludedPath(targetPath: String, pathMethod: String): Boolean {
        val isExcludePattern = excludePatterns.any { it.matches(pathMatcher, targetPath, pathMethod) }
        val isNotIncludePattern = includePatterns.none { it.matches(pathMatcher, targetPath, pathMethod) }
        return isExcludePattern || isNotIncludePattern
    }

    fun addIncludePatterns(path: String, vararg methods: HttpMethod) {
        methods.forEach { includePatterns.add(PathRequest(path, it)) }
    }

    fun addExcludePatterns(path: String, vararg methods: HttpMethod) {
        methods.forEach { excludePatterns.add(PathRequest(path, it)) }
    }
}