package com.voice.domain.voicememo.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.voice.domain.voicememo.dto.SpeechRequest
import com.voice.domain.voicememo.dto.TranscriptionRequest
import com.voice.domain.voicememo.dto.TranscriptionResponse
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.HttpStatus
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.apache.hc.core5.http.io.entity.StringEntity
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.IOException
import java.net.SocketTimeoutException

@Component
class GroqApiClient(
    private val httpClient: CloseableHttpClient,
    @Value("\${groq.api.key}") private val apiKey: String,
    @Value("\${groq.api.base-url}") private val baseUrl: String,
    @Value("\${groq.api.stt-endpoint}") private val sttEndpoint: String,
    @Value("\${groq.api.tts-endpoint}") private val ttsEndpoint: String
) {
    private val mapper = jacksonObjectMapper()

    companion object {
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY_MS = 1000L
    }

    fun transcribe(request: TranscriptionRequest): TranscriptionResponse {
        val url = "$baseUrl$sttEndpoint"

        repeat(MAX_RETRIES) { attempt ->
            try {
                val httpPost = HttpPost(url).apply {
                    addHeader("Authorization", "Bearer $apiKey")
                    addHeader("User-Agent", "VoiceMemo/1.0")
                }

                val entity = MultipartEntityBuilder.create()
                    .addBinaryBody("file", request.audioFile, ContentType.DEFAULT_BINARY, request.audioFile.name)
                    .addTextBody("model", "whisper-large-v3-turbo")
                    .addTextBody("language", request.language ?: "ko")
                    .addTextBody("response_format", "json")
                    .addTextBody("temperature", "0.0")
                    .build()

                httpPost.entity = entity

                val result = httpClient.execute(httpPost) { response ->
                    val status = response.code
                    val responseBody = EntityUtils.toString(response.entity)

                    println("STT API Response - Status: $status")

                    when (status) {
                        HttpStatus.SC_OK -> {
                            println("STT API Success")
                            mapper.readValue<TranscriptionResponse>(responseBody)
                        }
                        HttpStatus.SC_BAD_REQUEST -> {
                            val errorMessage = parseErrorMessage(responseBody)
                            throw IllegalArgumentException("잘못된 요청: $errorMessage")
                        }
                        HttpStatus.SC_UNAUTHORIZED -> {
                            throw IllegalStateException("API 키가 유효하지 않습니다")
                        }
                        HttpStatus.SC_TOO_MANY_REQUESTS -> {
                            throw IllegalStateException("요청 한도 초과. 잠시 후 다시 시도해주세요")
                        }
                        HttpStatus.SC_INTERNAL_SERVER_ERROR -> {
                            if (attempt < MAX_RETRIES - 1) {
                                println("Server error, retrying... (attempt ${attempt + 1})")
                                Thread.sleep(RETRY_DELAY_MS * (attempt + 1))
                                null
                            } else {
                                throw IllegalStateException("서버 오류가 발생했습니다")
                            }
                        }
                        else -> {
                            throw IllegalStateException("STT API failed with status $status: $responseBody")
                        }
                    }
                }

                if (result != null) {
                    return result
                }
            } catch (e: SocketTimeoutException) {
                if (attempt < MAX_RETRIES - 1) {
                    println("Timeout, retrying... (attempt ${attempt + 1})")
                    Thread.sleep(RETRY_DELAY_MS * (attempt + 1))
                } else {
                    throw IllegalStateException("요청 시간 초과. 네트워크 연결을 확인해주세요")
                }
            } catch (e: IOException) {
                if (attempt < MAX_RETRIES - 1) {
                    println("IO Exception, retrying... (attempt ${attempt + 1}): ${e.message}")
                    Thread.sleep(RETRY_DELAY_MS * (attempt + 1))
                } else {
                    throw IllegalStateException("네트워크 오류: ${e.message}")
                }
            }
        }

        throw IllegalStateException("STT API 호출이 최대 재시도 횟수를 초과했습니다")
    }

    fun generateSpeech(request: SpeechRequest): ByteArray {
        val url = "$baseUrl$ttsEndpoint"

        repeat(MAX_RETRIES) { attempt ->
            try {
                val httpPost = HttpPost(url).apply {
                    addHeader("Authorization", "Bearer $apiKey")
                    addHeader("Content-Type", "application/json")
                    addHeader("User-Agent", "VoiceMemo/1.0")
                }

                val requestBody = createTtsRequestBody(request)
                httpPost.setEntity(StringEntity(requestBody, ContentType.APPLICATION_JSON))

                val result = httpClient.execute(httpPost) { response ->
                    val status = response.code

                    println("TTS API Response - Status: $status, Model: ${request.model}, Voice: ${request.voice}")

                    when (status) {
                        HttpStatus.SC_OK -> {
                            val data = EntityUtils.toByteArray(response.entity)
                            println("TTS response data size: ${data.size} bytes")

                            if (data.isEmpty()) {
                                throw IllegalStateException("TTS API가 빈 데이터를 반환했습니다")
                            }

                            data
                        }
                        else -> {
                            val errorBody = EntityUtils.toString(response.entity)
                            val errorMessage = parseErrorMessage(errorBody)

                            if (errorBody.contains("model_terms_required")) {
                                throw IllegalStateException("PlayAI 모델 사용을 위해 약관 동의가 필요합니다. Groq 콘솔에서 약관에 동의하거나 다른 음성을 선택해주세요.")
                            }
                            else if (errorBody.contains("voice must be one of the following voices")) {
                                throw IllegalArgumentException("잘못된 요청: " + errorMessage)
                            }
                            else if (errorBody.contains("invalid_model")) {
                                throw IllegalArgumentException("잘못된 요청: ${request.model}은(는) 유효하지 않은 모델입니다. Groq API 문서를 확인하세요.")
                            }

                            throw IllegalStateException("TTS API failed with status $status: $errorMessage")
                        }
                    }
                }

                if (result != null) {
                    return result
                }
            } catch (e: SocketTimeoutException) {
                if (attempt < MAX_RETRIES - 1) {
                    println("Timeout, retrying... (attempt ${attempt + 1})")
                    Thread.sleep(RETRY_DELAY_MS * (attempt + 1))
                } else {
                    throw IllegalStateException("요청 시간 초과. 네트워크 연결을 확인해주세요")
                }
            } catch (e: IOException) {
                if (attempt < MAX_RETRIES - 1) {
                    println("IO Exception, retrying... (attempt ${attempt + 1}): ${e.message}")
                    Thread.sleep(RETRY_DELAY_MS * (attempt + 1))
                } else {
                    throw IllegalStateException("네트워크 오류: ${e.message}")
                }
            }
        }

        throw IllegalStateException("TTS API 호출이 최대 재시도 횟수를 초과했습니다")
    }

    private fun createTtsRequestBody(request: SpeechRequest): String {
        val requestMap = mutableMapOf<String, Any>(
            "model" to request.model,
            "input" to request.text,
            "voice" to request.voice,
            "response_format" to "wav"
        )

        when (request.model) {
            "playai-tts" -> {
                requestMap["speed"] = 1.0
            }
        }

        return mapper.writeValueAsString(requestMap)
    }

    private fun parseErrorMessage(responseBody: String): String {
        return try {
            val errorResponse = mapper.readTree(responseBody)
            errorResponse.get("error")?.get("message")?.asText() ?: responseBody
        } catch (e: Exception) {
            responseBody
        }
    }
}