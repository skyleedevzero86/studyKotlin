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
        다음 제공된 지식 정보를 바탕으로 사용자의 질문에 답변해 주세요.
        
        규칙:
        1. 지식 정보에 질문에 대한 답변이 있다면, 그 정보를 활용하여 정확하고 상세하게 답변하세요.
        2. 지식 정보에 질문에 대한 답변이 없다면, 일반적인 AI 지식으로 답변하거나 관련된 정보를 제공하세요.
        3. 답변은 자연스럽게 흘러가도록 하되, "지식에 따르면" 등의 표현은 사용하지 마세요.
        4. 답변은 친근하고 도움이 되는 톤으로 작성하세요.
        5. 지식 정보가 여러 개 있다면, 모든 관련 정보를 종합하여 답변하세요.
        6. 절대 "현재 가지고 있는 지식으로는 이 질문에 답변할 수 없습니다"라는 메시지를 출력하지 마세요.
        7. 항상 유용하고 도움이 되는 답변을 제공하세요.

        【지식 정보】
        {context}
                    
        【사용자 질문】
        {question}
        
        위 지식 정보를 바탕으로 답변해 주세요.
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
        val searchResults = documentService.doSearch(question)

        logger.info("검색된 문서 수: ${searchResults.size}")

        val context = if (searchResults.isNotEmpty()) {
            searchResults.joinToString("\n---\n") { document: Document ->
                val fileName = document.metadata["fileName"] ?: "알 수 없는 문서"
                "[$fileName]\n${document.content}"
            }
        } else {
            "관련된 지식 정보를 찾을 수 없습니다."
        }

        logger.info("RAG 컨텍스트 생성: ${context.length}자")
        logger.debug("RAG 컨텍스트 내용: $context")

        val promptContent = RAG_PROMPT_TEMPLATE
            .replace("{context}", context)
            .replace("{question}", question)

        return Prompt(promptContent)
    }
}