package com.azure.domain.service.impl

import com.azure.domain.entity.UserUseAiQuestion
import com.azure.domain.service.UserUseAiQuestionService
import com.azure.global.mapper.UserUseAiQuestionMapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import org.springframework.stereotype.Service
import java.sql.Timestamp

@Service
class UserUseAiQuestionServiceImpl : ServiceImpl<UserUseAiQuestionMapper, UserUseAiQuestion>(), UserUseAiQuestionService {

    // 사용자 질문 저장
    override fun saveUserQuestion(userId: String, question: String) {
        val userUseAiQuestion = UserUseAiQuestion().apply {
            this.userId = userId.toLong()
            this.userQuestion = question
            this.questionTime = Timestamp(System.currentTimeMillis())
            this.status = 1
        }
        save(userUseAiQuestion)
    }
}