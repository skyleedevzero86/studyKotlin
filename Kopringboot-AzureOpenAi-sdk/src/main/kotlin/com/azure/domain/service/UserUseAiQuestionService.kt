package com.azure.domain.service

import com.azure.domain.entity.UserUseAiQuestion
import com.baomidou.mybatisplus.extension.service.IService


/**
 * 사용자 AI 질문 서비스 인터페이스
 */
interface UserUseAiQuestionService : IService<UserUseAiQuestion> {

    // 사용자 질문 저장
    fun saveUserQuestion(userId: String, question: String)
}