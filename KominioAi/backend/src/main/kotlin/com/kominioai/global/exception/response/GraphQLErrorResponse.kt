package com.kominioai.global.exception.response

import java.time.Instant

data class GraphQLErrorResponse(
    override val timestamp: Instant,
    override val requestId: String?,
    override val errorCode: String,
    val message: String,
    val locations: List<Location>? = null,
    val path: List<Any>? = null,
    val extensions: Map<String, Any> = emptyMap()
) : ErrorResponse