package com.kominioai.domain.bulletin.domain.model

enum class PostCategory(val displayName: String, val description: String) {
    ANNOUNCEMENT("공지사항", "시스템 공지사항"),
    COMMUNITY("커뮤니티", "사용자 커뮤니티"),
    QNA("Q&A", "질문과 답변")
}
