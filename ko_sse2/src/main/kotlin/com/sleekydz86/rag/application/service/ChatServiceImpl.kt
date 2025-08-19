package com.sleekydz86.rag.application.service

import com.sleekydz86.rag.domain.model.ChatEntity
import com.sleekydz86.rag.infrastructure.external.SSEServer
import com.sleekydz86.rag.infrastructure.external.sse.SSEMsgType
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.ChatClient
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.document.Document
import org.springframework.stereotype.Service

@Service
class ChatServiceImpl(
    private val chatClient: ChatClient,
    private val documentService: DocumentService
) : ChatService {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val RAG_PROMPT_TEMPLATE = """
            다음 제공된 상황 정보를 바탕으로 사용자의 질문에 답변해 주세요.
            규칙:
            1. 답변 시 상황 정보를 충분히 활용하되, "상황에 따르면" 또는 "지식베이스에 따르면" 등의 표현은 직접적으로 언급하지 마세요.
            2. 상황 정보에 질문에 대한 충분한 답변이 없다면 명확히 "현재 가지고 있는 지식으로는 이 질문에 답변할 수 없습니다."라고 알려주세요.
            3. 답변은 직접적이고 명확하며 관련성이 있어야 합니다.

            【상황 정보】
            {context}
                        
            【질문】
            {question}
        """
    }

    override fun streamChat(chatEntity: ChatEntity) {
        val (userId, question, _, useKnowledgeBase) = chatEntity

        val prompt = if (useKnowledgeBase) {
            logger.info("【사용자: $userId】지식베이스 모드로 질문 중입니다.")
            createRAGPrompt(question)
        } else {
            logger.info("【사용자: $userId】일반 모드로 질문 중입니다.")
            Prompt(question)
        }

        try {
            val response = chatClient.call(prompt)
            val content = response.result?.output?.content ?: ""

            if (content.isNotEmpty()) {
                val chunks = content.chunked(100)
                chunks.forEach { chunk ->
                    SSEServer.sendMsg(userId, chunk, SSEMsgType.ADD)
                    Thread.sleep(50)
                }
            }

            logger.info("【사용자: $userId】응답이 성공적으로 완료되었습니다.")
            SSEServer.sendMsg(userId, "done", SSEMsgType.FINISH)
            SSEServer.close(userId)

        } catch (error: Exception) {
            logger.error("【사용자: $userId】AI 처리 중 오류 발생: ${error.message}", error)
                SSEServer.sendMsg(userId, "죄송합니다. 서비스에 문제가 발생했습니다. 잠시 후 다시 시도해 주세요.", SSEMsgType.FINISH)
                SSEServer.close(userId)
            }
    }

    private fun createRAGPrompt(question: String): Prompt {
        val context = documentService.doSearch(question)
            .takeIf { it.isNotEmpty() }
            ?.joinToString("\n---\n") { document: Document -> document.content }
            ?: "관련된 지식베이스 정보를 찾을 수 없습니다."

        val promptContent = RAG_PROMPT_TEMPLATE
            .replace("{context}", context)
            .replace("{question}", question)

        return Prompt(promptContent)
    }
}