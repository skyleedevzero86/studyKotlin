package com.voice.domain.speechapp.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.voice.domain.global.util.KoreanRomanizer
import com.voice.domain.speech.TtsOptions
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Controller
class SpeechController(
    private val ttsOptions: TtsOptions,
    private val uploadDir: File
) {

    @Value("\${speech.api.key}")
    private lateinit var apiKey: String

    @Value("\${speech.api.base-url}")
    private lateinit var baseUrl: String

    private val restTemplate = RestTemplate()
    private val mapper = jacksonObjectMapper()

    @GetMapping("/")
    fun index(model: Model): String {
        return "audioprocessor/speechapp"
    }

    @PostMapping("/speech-to-text")
    @ResponseBody
    fun speechToText(
        @RequestParam("audioFile") audioFile: MultipartFile,
        @RequestParam("model", defaultValue = "whisper-large-v3-turbo") model: String,
        @RequestParam("language", defaultValue = "ko") language: String
    ): Map<String, Any?> {
        return try {
            if (audioFile.isEmpty) {
                return mapOf(
                    "success" to false,
                    "error" to "음성 파일이 비어있습니다."
                )
            }

            println("STT Request - File: ${audioFile.originalFilename}, Size: ${audioFile.size} bytes, Model: $model, Language: $language")

            if (!uploadDir.exists()) {
                uploadDir.mkdirs()
            }

            val originalFilename = audioFile.originalFilename ?: "audio.wav"
            val extension = originalFilename.substringAfterLast('.', "wav")
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
            val safeExtension = if (extension.isNotBlank() && extension.length <= 10) extension else "wav"
            val savedFile = File(uploadDir, "input_$timestamp.$safeExtension")

            FileUtils.writeByteArrayToFile(savedFile, audioFile.bytes)
            println("Saved input file: ${savedFile.absolutePath}, size: ${savedFile.length()} bytes")

            val headers = HttpHeaders().apply {
                set("Authorization", "Bearer $apiKey")
                contentType = MediaType.MULTIPART_FORM_DATA
            }

            val body: MultiValueMap<String, Any> = LinkedMultiValueMap<String, Any>().apply {
                add("file", object : ByteArrayResource(audioFile.bytes) {
                    override fun getFilename(): String = originalFilename
                })
                add("model", model)
                add("language", language)
                add("response_format", "verbose_json")
            }

            val request = HttpEntity(body, headers)
            val response = restTemplate.postForEntity(
                "$baseUrl/audio/transcriptions",
                request,
                Map::class.java
            )

            println("STT API Response - Status: ${response.statusCode}")

            @Suppress("UNCHECKED_CAST")
            val responseBody = response.body as? Map<String, Any> ?: emptyMap()

            val text = responseBody["text"] as? String ?: ""
            println("STT Result - Text length: ${text.length}, Preview: ${text.take(100)}")

            if (text.isBlank()) {
                return mapOf(
                    "success" to false,
                    "error" to "음성에서 텍스트를 인식할 수 없습니다. 음성이 명확한지 확인해주세요."
                )
            }

            mapOf(
                "success" to true,
                "text" to text,
                "segments" to (responseBody["segments"] ?: emptyList<Any>()),
                "words" to (responseBody["words"] ?: emptyList<Any>()),
                "savedFile" to savedFile.name,
                "savedPath" to savedFile.absolutePath
            )
        } catch (e: Exception) {
            println("STT Error: ${e.message}")
            e.printStackTrace()
            mapOf(
                "success" to false,
                "error" to (e.message ?: "음성 변환 중 오류가 발생했습니다.")
            )
        }
    }

    @PostMapping("/text-to-speech")
    @ResponseBody
    fun textToSpeech(
        @RequestBody requestBodyMap: Map<String, Any>
    ): ResponseEntity<ByteArray> {
        return try {
            val headers = HttpHeaders().apply {
                set("Authorization", "Bearer $apiKey")
                contentType = MediaType.APPLICATION_JSON
            }

            val originalText = requestBodyMap["text"] as? String ?: ""
            if (originalText.isBlank()) {
                return ttsError("텍스트를 입력해주세요.", HttpStatus.BAD_REQUEST)
            }

            val modelKey = ttsOptions.resolveModelKey(requestBodyMap["model"] as? String)
            val model = ttsOptions.modelId(modelKey)
            val voice = ttsOptions.resolveVoice(modelKey, requestBodyMap["voice"] as? String)
            val processedText = KoreanRomanizer.preprocessTextForTTS(originalText).trim()

            if (processedText.length > TtsOptions.MAX_INPUT_CHARS) {
                return ttsError(
                    "TTS는 요청당 최대 ${TtsOptions.MAX_INPUT_CHARS}자까지 지원됩니다. 현재 ${processedText.length}자입니다.",
                    HttpStatus.BAD_REQUEST
                )
            }

            val requestBody = mapOf(
                "model" to model,
                "input" to processedText,
                "voice" to voice,
                "response_format" to TtsOptions.RESPONSE_FORMAT
            )

            val request = HttpEntity(requestBody, headers)
            val response = restTemplate.postForEntity(
                "$baseUrl/audio/speech",
                request,
                ByteArray::class.java
            )

            if (response.body != null && response.body!!.isNotEmpty()) {
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs()
                }

                val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                val safeVoice = voice.replace("-", "_").take(20)
                val outputFile = File(uploadDir, "output_${safeVoice}_$timestamp.wav")

                FileUtils.writeByteArrayToFile(outputFile, response.body!!)
                println("Saved output file: ${outputFile.absolutePath}, size: ${outputFile.length()} bytes")
            }

            val responseHeaders = HttpHeaders().apply {
                contentType = MediaType.parseMediaType("audio/wav")
                setContentDispositionFormData("attachment", "speech.wav")
            }

            ResponseEntity(response.body, responseHeaders, HttpStatus.OK)
        } catch (e: IllegalArgumentException) {
            println("TTS Validation Error: ${e.message}")
            ttsError(e.message ?: "TTS 요청이 올바르지 않습니다.", HttpStatus.BAD_REQUEST)
        } catch (e: HttpClientErrorException) {
            val errorMessage = parseProviderErrorMessage(e.responseBodyAsString, e.message)

            println("TTS API Error: $errorMessage")
            ttsError(errorMessage, e.statusCode)
        } catch (e: Exception) {
            println("TTS Error: ${e.message}")
            e.printStackTrace()
            ttsError(e.message ?: "음성 생성 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    private fun ttsError(message: String, status: HttpStatusCode): ResponseEntity<ByteArray> {
        val headers = HttpHeaders().apply {
            contentType = MediaType.TEXT_PLAIN
        }
        return ResponseEntity.status(status).headers(headers).body(message.toByteArray(Charsets.UTF_8))
    }

    private fun parseProviderErrorMessage(responseBody: String, fallback: String?): String {
        if (responseBody.contains("model_terms_required") || responseBody.contains("terms acceptance", ignoreCase = true)) {
            return "TTS 모델 약관 동의가 필요합니다. 콘솔에서 모델 약관에 동의한 뒤 다시 시도해주세요."
        }

        if (responseBody.contains("model_decommissioned")) {
            return "요청한 TTS 모델은 더 이상 지원되지 않습니다. 설정을 확인해주세요."
        }

        return try {
            mapper.readTree(responseBody)
                .path("error")
                .path("message")
                .asText(fallback ?: "TTS API 오류가 발생했습니다.")
        } catch (e: Exception) {
            fallback ?: "TTS API 오류가 발생했습니다."
        }
    }

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
