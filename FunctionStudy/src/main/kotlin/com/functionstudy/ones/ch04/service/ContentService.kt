package com.functionstudy.ones.ch04.service

import com.functionstudy.ones.ch04.dto.Request
import com.functionstudy.ones.ch04.dto.Response

fun fetchListContent(request: Request): List<String> {
    println("Fetching list content for request: ${request.content}")
    return listOf("Item 1", "Item 2", "Item 3")
}

fun renderHtml(request: Request): String {
    println("Rendering HTML for request: ${request.content}")
    return "<html><body><h1>${request.content}</h1></body></html>"
}

fun createResponse(request: Request): Response {
    println("Creating response for request: ${request.content}")
    return Response("Response for ${request.content}")
}
