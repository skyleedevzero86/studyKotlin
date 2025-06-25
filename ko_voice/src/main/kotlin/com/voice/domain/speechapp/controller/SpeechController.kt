package com.voice.domain.speechapp.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

@Controller
class SpeechController {

    @Value("\${groq.api.key}")
    private lateinit var apiKey: String

    @Value("\${groq.api.base-url}")
    private lateinit var baseUrl: String

    private val restTemplate = RestTemplate()

    @GetMapping("/")
    fun index(model: Model): String {
        return "audioprocessor/speechapp"
    }

    // 음성을 텍스트로 변환 (Speech-to-Text)
    @PostMapping("/speech-to-text")
    @ResponseBody
    fun speechToText(
        @RequestParam("audioFile") audioFile: MultipartFile,
        @RequestParam("model", defaultValue = "whisper-large-v3-turbo") model: String,
        @RequestParam("language", defaultValue = "ko") language: String
    ): Map<String, Any?> {
        return try {
            val headers = HttpHeaders().apply {
                set("Authorization", "Bearer $apiKey")
                contentType = MediaType.MULTIPART_FORM_DATA
            }

            val body: MultiValueMap<String, Any> = LinkedMultiValueMap<String, Any>().apply {
                add("file", object : ByteArrayResource(audioFile.bytes) {
                    override fun getFilename(): String = audioFile.originalFilename ?: "audio.wav"
                })
                add("model", model)
                add("language", language)
                add("response_format", "verbose_json")
                add("timestamp_granularities", "word")
                add("timestamp_granularities", "segment")
            }

            val request = HttpEntity(body, headers)
            val response = restTemplate.postForEntity(
                "$baseUrl/audio/transcriptions",
                request,
                Map::class.java
            )

            @Suppress("UNCHECKED_CAST")
            val responseBody = response.body as? Map<String, Any> ?: emptyMap()

            mapOf(
                "success" to true,
                "text" to (responseBody["text"] ?: ""),
                "segments" to (responseBody["segments"] ?: emptyList<Any>()),
                "words" to (responseBody["words"] ?: emptyList<Any>())
            )
        } catch (e: Exception) {
            mapOf(
                "success" to false,
                "error" to e.message
            )
        }
    }

    // 텍스트를 음성으로 변환 (Text-to-Speech)
    @PostMapping("/text-to-speech")
    @ResponseBody
    fun textToSpeech(
        @RequestBody requestBodyMap: Map<String, Any> // JSON 요청 본문을 Map으로 받음
    ): ResponseEntity<ByteArray> {
        return try {
            val headers = HttpHeaders().apply {
                set("Authorization", "Bearer $apiKey")
                contentType = MediaType.APPLICATION_JSON
            }

            // Map에서 값 추출
            val text = requestBodyMap["text"] as? String ?: ""
            val voice = requestBodyMap["voice"] as? String ?: "Fritz-PlayAI"
            val model = requestBodyMap["model"] as? String ?: "playai-tts"

            val requestBody = mapOf(
                "model" to model,
                "input" to text,
                "voice" to voice,
                "response_format" to "wav"
            )

            val request = HttpEntity(requestBody, headers)
            val response = restTemplate.postForEntity(
                "$baseUrl/audio/speech", // Groq API 기준 endpoint
                request,
                ByteArray::class.java
            )

            val responseHeaders = HttpHeaders().apply {
                contentType = MediaType.parseMediaType("audio/wav")
                setContentDispositionFormData("attachment", "speech.wav")
            }

            ResponseEntity(response.body, responseHeaders, HttpStatus.OK)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    // 음성 파일 저장
    @PostMapping("/save-audio")
    @ResponseBody
    fun saveAudio(
        @RequestParam("audioFile") audioFile: MultipartFile,
        @RequestParam("filename", required = false) filename: String?
    ): Map<String, Any?> {
        return try {
            val uploadsDir = File("uploads")
            if (!uploadsDir.exists()) {
                uploadsDir.mkdirs()
            }

            val savedFilename = filename ?: "audio_${System.currentTimeMillis()}.wav"
            val filePath = Paths.get("uploads", savedFilename)
            Files.write(filePath, audioFile.bytes)

            mapOf(
                "success" to true,
                "filename" to savedFilename,
                "path" to filePath.toString()
            )
        } catch (e: Exception) {
            mapOf(
                "success" to false,
                "error" to e.message
            )
        }
    }

    // 저장된 음성 파일 목록 조회
    @GetMapping("/audio-files")
    @ResponseBody
    fun getAudioFiles(): Map<String, Any?> {
        return try {
            val uploadsDir = File("uploads")
            val files = if (uploadsDir.exists()) {
                uploadsDir.listFiles()?.filter { it.isFile }?.map { it.name } ?: emptyList()
            } else {
                emptyList()
            }

            mapOf(
                "success" to true,
                "files" to files
            )
        } catch (e: Exception) {
            mapOf(
                "success" to false,
                "error" to e.message
            )
        }
    }

    // 저장된 음성 파일 재생
    @GetMapping("/play-audio/{filename}")
    fun playAudio(@PathVariable filename: String): ResponseEntity<ByteArray> {
        return try {
            val filePath = Paths.get("uploads", filename)
            val audioBytes = Files.readAllBytes(filePath)

            val headers = HttpHeaders().apply {
                contentType = MediaType.parseMediaType("audio/wav")
            }

            ResponseEntity(audioBytes, headers, HttpStatus.OK)
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }
}
