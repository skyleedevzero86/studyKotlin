package com.sleekydz86.service.impl

import com.sleekydz86.bean.ChatEntity
import com.sleekydz86.enums.SSEMsgType
import com.sleekydz86.service.ChatService
import com.sleekydz86.utils.SSEServer
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicBoolean
import jakarta.annotation.PostConstruct
import kotlin.random.Random

@Service
class ChatServiceImpl : ChatService {

    private val logger = LoggerFactory.getLogger(ChatServiceImpl::class.java)

    @Value("\${huggingface.chat.model-name:microsoft/DialoGPT-small}")
    private lateinit var modelName: String

    @Value("\${huggingface.chat.max-length:200}")
    private var maxLength: Int = 200

    @Value("\${huggingface.chat.temperature:0.7}")
    private var temperature: Double = 0.7

    private val personality = mapOf(
        "greeting" to listOf(
            "안녕하세요! 🙋‍♀️ 오늘 하루는 어떠세요?",
            "반가워요! 😊 뭔가 재미있는 얘기 없나요?",
            "안녕! 👋 좋은 하루 보내고 계신가요?"
        ),
        "tired_responses" to listOf(
            "아 정말 피곤하시겠어요 😴 요즘 밤늦게 자셨나요?",
            "졸음이 몰려오는군요... 커피 한 잔 어떠세요? ☕",
            "피곤하실 때는 잠깐 휴식을 취하시는 게 좋을 것 같아요 💤",
            "아 졸리시겠다... 충분한 수면이 제일 중요해요! 😌"
        ),
        "encouragement" to listOf(
            "힘내세요! 조금만 더 버티면 될 거예요 💪",
            "그래도 여기까지 오신 것만으로도 대단하세요!",
            "오늘 하루도 수고 많으셨어요 👏",
            "가끔은 쉬어가는 것도 필요해요 🌸"
        ),
        "weather_chat" to listOf(
            "오늘 날씨는 어때요? 바깥 공기 좀 쐬시는 것도 좋을 듯해요 🌤️",
            "날씨가 좋으면 기분도 좋아지잖아요!",
            "비 오는 날엔 집에서 따뜻한 차 마시며 쉬는 게 최고죠 ☔"
        ),
        "casual_responses" to listOf(
            "그러게요! 저도 그런 생각 해봤어요 🤔",
            "정말요? 흥미롭네요! 더 자세히 얘기해주세요 ✨",
            "아하! 그런 일이 있으셨구나요 😮",
            "와 그건 정말 신기하네요! 🌟"
        )
    )

    private val emotionKeywords = mapOf(
        "tired" to listOf("졸려", "피곤", "잠", "힘들", "지쳐", "sleepy", "tired"),
        "sad" to listOf("슬퍼", "우울", "속상", "힘들", "sad", "depressed"),
        "happy" to listOf("기뻐", "좋아", "신나", "행복", "happy", "excited", "great"),
        "angry" to listOf("화나", "짜증", "분노", "angry", "mad", "frustrated"),
        "hungry" to listOf("배고파", "밥", "음식", "먹고싶", "hungry"),
        "bored" to listOf("심심", "지루", "재미없", "bored"),
        "stressed" to listOf("스트레스", "압박", "부담", "stressed")
    )

    @PostConstruct
    fun init() {
        logger.info("대화형 AI 초기화 완료 - 모델: {}, Temperature: {}, MaxLength: {}",
            modelName, temperature, maxLength)
    }

    override fun doChat(chatEntity: ChatEntity) {
        val userId = chatEntity.currentUserName
        val prompt = chatEntity.message
        val errorHandled = AtomicBoolean(false)

        try {
            logger.info("사용자 {} 대화 시작: {}", userId, prompt)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    generateConversationalResponse(userId, prompt, errorHandled)
                } catch (e: Exception) {
                    if (errorHandled.compareAndSet(false, true)) {
                        logger.error("대화 생성 오류: {}", e.message)
                        SSEServer.sendMsg(userId, "앗, 잠깐 생각이 꼬였네요 😅 다시 말씀해 주시겠어요?", SSEMsgType.FINISH)
                        SSEServer.close(userId)
                    }
                }
            }
        } catch (e: Exception) {
            if (errorHandled.compareAndSet(false, true)) {
                logger.error("대화 처리 중 예외 발생: {}", e.message, e)
                SSEServer.sendMsg(userId, "어? 뭔가 이상하네요... 🤔 다시 시도해 주세요!", SSEMsgType.FINISH)
                SSEServer.close(userId)
            }
        }
    }

    private suspend fun generateConversationalResponse(userId: String, prompt: String, errorHandled: AtomicBoolean) {
        delay(100)
        val detectedEmotion = detectEmotion(prompt)
        logger.debug("감정 분석 결과: {}", detectedEmotion)
        delay(150)
        val response = generateContextualResponse(prompt, detectedEmotion)
        val tokens = tokenizeForConversation(response)

        for ((index, token) in tokens.withIndex()) {
            if (errorHandled.get()) break
            val thinkingDelay = when {
                index == 0 -> Random.nextLong(300, 600)
                index < 3 -> Random.nextLong(150, 300)
                index < tokens.size - 3 -> Random.nextLong(80, 180)
                else -> Random.nextLong(120, 250)
            }
            delay(thinkingDelay)
            val chunk = if (index == tokens.size - 1) token else "$token "
            SSEServer.sendMsg(userId, chunk, SSEMsgType.ADD)
        }

        if (!errorHandled.get()) {
            delay(200)
            logger.info("사용자 {} 대화 완료", userId)
            SSEServer.sendMsg(userId, "", SSEMsgType.FINISH)
            SSEServer.close(userId)
        }
    }

    private fun detectEmotion(text: String): String {
        val lowerText = text.lowercase()
        return emotionKeywords.entries.find { (emotion, keywords) ->
            keywords.any { keyword -> lowerText.contains(keyword) }
        }?.key ?: "neutral"
    }

    private fun generateContextualResponse(prompt: String, emotion: String): String {
        val lowercasePrompt = prompt.lowercase()
        return when {
            lowercasePrompt.contains("안녕") || lowercasePrompt.contains("hello") || lowercasePrompt.contains("hi") -> {
                personality["greeting"]!!.random()
            }
            emotion == "tired" -> {
                val response = personality["tired_responses"]!!.random()
                val encouragement = personality["encouragement"]!!.random()
                "$response $encouragement"
            }
            emotion == "sad" -> {
                "아 무슨 일 있으셨나요? 😢 속상한 일이 있으시면 얘기해 주세요. 들어드릴게요!"
            }
            emotion == "happy" -> {
                "와! 좋은 일이 있으셨나보네요! 😄✨ 기분 좋은 건 나눠야 더 커진다잖아요! 뭔 일이에요?"
            }
            emotion == "angry" -> {
                "어? 뭔가 화나는 일이 있으셨나요? 😤 스트레스 받는 일 있으시면 푸념이라도 해보세요!"
            }
            emotion == "hungry" -> {
                "아 배고프시겠어요! 🍽️ 뭐 드시고 싶으세요? 맛있는 거 드시고 힘내세요!"
            }
            emotion == "bored" -> {
                "심심하시구나! 🙃 그럼 재미있는 얘기라도 해볼까요? 아니면 뭔가 새로운 걸 해보시는 건 어때요?"
            }
            emotion == "stressed" -> {
                "스트레스 받으시는군요 😰 일단 깊게 숨 한 번 쉬어보세요~ 힘든 일 있으시면 얘기해 주세요!"
            }
            lowercasePrompt.contains("sse") || lowercasePrompt.contains("스트리밍") -> {
                "아! SSE 얘기하시는군요! 😊 Server-Sent Events는 정말 신기한 기술이에요. 지금 우리가 대화하는 것도 SSE로 실시간 스트리밍되고 있거든요! 마치 채팅하는 것처럼 자연스럽죠? ✨"
            }
            lowercasePrompt.contains("허깅페이스") || lowercasePrompt.contains("hugging") -> {
                "허깅페이스 얘기시는군요! 🤗 정말 멋진 플랫폼이에요. 지금 저도 $modelName 모델 기반으로 대화하고 있어요! AI 기술이 정말 많이 발전했죠?"
            }
            lowercasePrompt.contains("코틀린") || lowercasePrompt.contains("kotlin") -> {
                "코틀린! 💎 정말 깔끔한 언어죠! Java보다 훨씬 간결하고 읽기 쉬워요. 특히 null safety 기능이 개발할 때 진짜 도움 많이 돼요!"
            }
            lowercasePrompt.contains("모델") || lowercasePrompt.contains("너는") || lowercasePrompt.contains("당신은") -> {
                "저는 $modelName 기반으로 만들어진 대화형 AI에요! 🤖 여러분과 자연스럽게 대화하는 걸 좋아해요. 궁금한 게 있으면 뭐든 물어보세요!"
            }
            prompt.length <= 3 -> {
                val casual = personality["casual_responses"]!!.random()
                "$casual 좀 더 자세히 얘기해 주세요!"
            }
            else -> {
                val starters = listOf(
                    "그런 얘기시는군요! 🤔",
                    "음 흥미로운데요? 🌟",
                    "아하! 그렇군요 😊",
                    "정말요? 신기하네요! ✨"
                )
                val continuations = listOf(
                    "더 자세히 얘기해 주시겠어요?",
                    "어떤 기분이셨어요?",
                    "그 다음엔 어떻게 되었나요?",
                    "비슷한 경험 있으신가요?",
                    "어떻게 생각하세요?"
                )
                "${starters.random()} ${continuations.random()}"
            }
        }
    }

    private fun tokenizeForConversation(response: String): List<String> {
        return response
            .replace(Regex("([.!?😊😄😢😤🤔🌟✨🙃😰🤗💎🤖])"), " $1 ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
    }
}