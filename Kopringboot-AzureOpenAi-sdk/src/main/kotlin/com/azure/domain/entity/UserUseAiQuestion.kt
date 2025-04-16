package com.azure.domain.entity



import com.baomidou.mybatisplus.annotation.*
import com.azure.global.base.BaseEntity
import java.sql.Timestamp

/**
 * 사용자 AI 질문 이력 엔티티
 */
@TableName("tb_user_question")
data class UserUseAiQuestion(


    // 사용자 ID
    var userId: Long? = null,

    // 사용자가 입력한 질문
    var userQuestion: String? = null,

    // 질문 시간
    var questionTime: Timestamp? = null,

    // 상태: 0 - 삭제됨, 1 - 정상, 2 - 금지됨
    var status: Int? = null,

) : BaseEntity(){}