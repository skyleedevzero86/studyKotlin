package com.voice.domain.voicememo.controller

import com.voice.domain.speech.TtsOptions
import com.voice.domain.voicememo.service.VoiceService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import java.io.File

@Controller
class VoiceController(
    private val voiceService: VoiceService,
    private val uploadDir: File
) {

    private val supportedVoices = TtsOptions.englishVoices

    @GetMapping("/voice")
    fun index(model: Model): String {
        addCommonAttributes(model)
        model.addAttribute("selectedVoice", "troy")
        return "voicememo/Voice"
    }

    @PostMapping("/transcribe")
    fun transcribeAudio(
        @RequestParam("audio", required = false) audio: MultipartFile?,
        @RequestParam("language") language: String,
        model: Model
    ): String {
        try {
            if (audio == null || audio.isEmpty) {
                model.addAttribute("error", "음성 파일이 제공되지 않았습니다.")
            } else {
                val originalFilename = audio.originalFilename ?: "audio"
                val extension = originalFilename.substringAfterLast('.', "wav")
                val transcript = voiceService.processSpeechToText(audio.bytes, language, extension)
                model.addAttribute("transcript", transcript)
                model.addAttribute("success", "음성이 성공적으로 텍스트로 변환되었습니다.")
            }
        } catch (e: Exception) {
            println("Transcription error: ${e.message}")
            e.printStackTrace()
            model.addAttribute("error", "음성 변환 중 오류 발생: ${e.message}")
        }

        addCommonAttributes(model)
        return "voicememo/Voice"
    }

    @PostMapping("/generate")
    fun generateSpeech(
        @RequestParam("text") text: String,
        @RequestParam("voice") voice: String,
        model: Model
    ): String {
        try {
            if (text.isBlank()) {
                model.addAttribute("error", "텍스트를 입력해주세요.")
            } else if (voice !in supportedVoices) {
                model.addAttribute("error", "선택하신 음성(${voice})은 지원하지 않습니다. 목록에서 선택해주세요.")
            } else {
                val audioFileName = voiceService.processTextToSpeech(text, voice)
                model.addAttribute("audioFile", "/audio/$audioFileName")
                model.addAttribute("success", "음성이 성공적으로 생성되었습니다.")
                model.addAttribute("generatedText", text)
                model.addAttribute("selectedVoice", voice)
                println("Generated audio file: $audioFileName at ${uploadDir.absolutePath}")
            }
        } catch (e: Exception) {
            println("TTS error: ${e.message}")
            e.printStackTrace()

            val errorMessage = when {
                e.message?.contains("model_terms_required") == true ->
                    "TTS 모델 사용을 위해 약관 동의가 필요합니다."
                e.message?.contains("model_decommissioned") == true ->
                    "요청한 TTS 모델은 더 이상 지원되지 않습니다. 설정을 확인해주세요."
                e.message?.contains("insufficient_quota") == true ->
                    "API 할당량이 부족합니다. 나중에 다시 시도해주세요."
                e.message?.contains("invalid_api_key") == true ->
                    "API 키가 유효하지 않습니다. 설정을 확인해주세요."
                e.message?.contains("voice must be one of the following voices") == true ->
                    "선택하신 음성은 지원하지 않습니다. 다른 음성을 선택해주세요."
                else -> "음성 생성 중 오류 발생: ${e.message}"
            }
            model.addAttribute("error", errorMessage)
        }

        addCommonAttributes(model)
        model.addAttribute("selectedVoice", if (voice in supportedVoices) voice else "troy")
        return "voicememo/Voice"
    }

    private fun addCommonAttributes(model: Model) {
        model.addAttribute("voices", supportedVoices)
        model.addAttribute("languages", listOf(
            "ko" to "한국어",
            "en" to "영어",
            "ja" to "일본어",
            "zh" to "중국어"
        ))
    }

    @GetMapping("/audio/{fileName}")
    fun serveAudio(@PathVariable fileName: String): ResponseEntity<ByteArray> {
        return try {
            val file = File(uploadDir, fileName)
            if (!file.exists()) {
                println("File not found: ${file.absolutePath}")
                return ResponseEntity.notFound().build()
            }

            println("Serving file: ${file.absolutePath}, size: ${file.length()} bytes")

            val contentType = when (file.extension.lowercase()) {
                "wav" -> MediaType.parseMediaType("audio/wav")
                "mp3" -> MediaType.parseMediaType("audio/mpeg")
                "ogg" -> MediaType.parseMediaType("audio/ogg")
                "m4a" -> MediaType.parseMediaType("audio/mp4")
                else -> MediaType.APPLICATION_OCTET_STREAM
            }

            ResponseEntity.ok()
                .contentType(contentType)
                .contentLength(file.length())
                .body(file.readBytes())
        } catch (e: Exception) {
            println("Error serving audio file: ${e.message}")
            ResponseEntity.internalServerError().build()
        }
    }
}
