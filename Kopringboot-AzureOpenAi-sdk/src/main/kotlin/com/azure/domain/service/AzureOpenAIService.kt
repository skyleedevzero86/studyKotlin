package com.azure.domain.service

import com.alibaba.fastjson2.JSON
import okhttp3.MediaType.Companion.toMediaType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.azure.ai.openai.OpenAIClient
import com.azure.ai.openai.OpenAIClientBuilder
import com.azure.core.credential.AzureKeyCredential
import com.azure.domain.dto.ChatRequest
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import java.util.concurrent.TimeUnit
import java.util.Objects
import okhttp3.Request

/**
 * AzureOpenAI 서비스 클래스
 */
class AzureOpenAIService(
    private val apiKey: String?,
    private val deployment: String?,
    private val endpoint: String?,
    private val apiVersion: String = "2023-03-15-preview"
) {

    private val logger: Logger = LoggerFactory.getLogger(AzureOpenAIService::class.java)
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.MINUTES)
        .writeTimeout(1, TimeUnit.MINUTES)
        .readTimeout(1, TimeUnit.MINUTES)
        .build()

    private val azureClient: OpenAIClient? = apiKey?.let {
        OpenAIClientBuilder()
            .endpoint(endpoint ?: "")
            .credential(AzureKeyCredential(it))
            .buildClient()
    }

    /**
     * 채팅 응답 완료 메서드
     */
    fun chatCompletion(request: ChatRequest): String {
        return callAzureOpenAIAPI("chat/completions", JSON.toJSONString(request), apiVersion)
    }

    /**
     * AzureOpenAI API 호출
     */
    private fun callAzureOpenAIAPI(operation: String, requestBodyString: String, apiVersion: String): String {
        val mediaType = "application/json".toMediaType()
        val url = "$endpoint/openai/deployments/$deployment/$operation?api-version=$apiVersion"
        logger.info("요청 내용: {}", requestBodyString)
        val body = RequestBody.create(mediaType, requestBodyString)
        val httpRequest = Request.Builder().url(url).post(body)
            .addHeader("api-key", apiKey ?: "").build()

        return client.newCall(httpRequest).execute().use { response ->
            if (!response.isSuccessful) throw RuntimeException("에러 응답: $response")
            Objects.requireNonNull(response.body)?.string() ?: throw RuntimeException("빈 응답 본문")
        }
    }
}