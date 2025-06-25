package com.voice.domain.voicememo.service

import com.voice.domain.voicememo.dto.SpeechRequest
import com.voice.domain.voicememo.dto.TranscriptionRequest
import org.apache.commons.io.FileUtils
import org.springframework.stereotype.Service
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class VoiceService(
    private val groqClient: GroqApiClient,
    private val uploadDir: File
) {

    companion object {
        private val TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        private const val MAX_FILE_SIZE = 25 * 1024 * 1024 // 25MB
        private const val MAX_TEXT_LENGTH = 4096 // 4KB
    }

    /**
     * 음성을 텍스트로 변환 및 파일 저장
     */
    fun processSpeechToText(audioData: ByteArray, language: String): String {
        // 파일 크기 검증
        if (audioData.size > MAX_FILE_SIZE) {
            throw IllegalArgumentException("음성 파일이 너무 큽니다. 최대 25MB까지 지원됩니다.")
        }

        if (audioData.isEmpty()) {
            throw IllegalArgumentException("음성 데이터가 비어있습니다.")
        }

        val timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER)
        val audioFile = File(uploadDir, "input_$timestamp.wav")

        return try {
            // 업로드 디렉토리 확인 및 생성
            if (!uploadDir.exists()) {
                uploadDir.mkdirs()
            }

            FileUtils.writeByteArrayToFile(audioFile, audioData)
            println("Saved input file: ${audioFile.absolutePath}, size: ${audioFile.length()} bytes")

            val request = TranscriptionRequest(audioFile, language)
            val response = groqClient.transcribe(request)

            // 변환 결과 검증
            if (response.text.isBlank()) {
                throw IllegalStateException("음성에서 텍스트를 인식할 수 없습니다.")
            }

            println("Transcription successful: ${response.text.take(100)}...")
            response.text

        } catch (e: Exception) {
            println("STT processing error: ${e.message}")
            throw e
        } finally {
            // 임시 파일 정리 (선택적)
            try {
                if (audioFile.exists()) {
                    // audioFile.delete() // 필요시 주석 해제
                }
            } catch (e: Exception) {
                println("Failed to delete temp file: ${e.message}")
            }
        }
    }

    /**
     * 텍스트를 음성으로 변환 및 파일 저장
     */
    fun processTextToSpeech(text: String, voice: String): String {
        // 텍스트 길이 검증
        if (text.length > MAX_TEXT_LENGTH) {
            throw IllegalArgumentException("텍스트가 너무 깁니다. 최대 ${MAX_TEXT_LENGTH}자까지 지원됩니다.")
        }

        if (text.isBlank()) {
            throw IllegalArgumentException("텍스트가 비어있습니다.")
        }

        val model = "playai-tts" // voice 값에 관계없이 PlayAI TTS 모델로 고정

        val request = SpeechRequest(model = model, text = text, voice = voice)

        return try {
            // 업로드 디렉토리 확인 및 생성
            if (!uploadDir.exists()) {
                uploadDir.mkdirs()
            }

            val speechData = groqClient.generateSpeech(request)

            if (speechData.isEmpty()) {
                throw IllegalStateException("TTS API에서 음성 데이터를 받지 못했습니다.")
            }

            val timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER)
            val outputFile = File(uploadDir, "output_${voice}_$timestamp.wav")

            FileUtils.writeByteArrayToFile(outputFile, speechData)
            println("Saved output file: ${outputFile.absolutePath}, size: ${outputFile.length()} bytes")

            // 파일 생성 확인
            if (!outputFile.exists() || outputFile.length() == 0L) {
                throw IllegalStateException("음성 파일 생성에 실패했습니다.")
            }

            outputFile.name

        } catch (e: Exception) {
            println("TTS processing error: ${e.message}")
            throw e
        }
    }


    /**
     * 업로드 디렉토리의 오래된 파일들을 정리
     */
    fun cleanupOldFiles(daysOld: Int = 7) {
        try {
            val cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)

            uploadDir.listFiles()?.forEach { file ->
                if (file.lastModified() < cutoffTime) {
                    val deleted = file.delete()
                    println("Cleaned up old file: ${file.name}, deleted: $deleted")
                }
            }
        } catch (e: Exception) {
            println("File cleanup error: ${e.message}")
        }
    }
}