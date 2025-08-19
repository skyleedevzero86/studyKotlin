package com.sleekydz86.rag.domain.model

data class ChatEntity(
    val currentUserName: String,
    val message: String,
    val botMsgId: String? = null,
    val useKnowledgeBase: Boolean = false
) {
    companion object {
        fun createDefault(userName: String, message: String) =
            ChatEntity(userName, message, useKnowledgeBase = false)

        fun createWithKnowledge(userName: String, message: String) =
            ChatEntity(userName, message, useKnowledgeBase = true)
    }
}
