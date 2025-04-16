package com.azure.global.mapper

import com.azure.domain.entity.UserUseAiQuestion
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import org.springframework.stereotype.Repository

/**
 * 사용자가 AI에게 질문한 내역을 처리하는 Mapper 인터페이스
 */
@Repository
interface UserUseAiQuestionMapper : BaseMapper<UserUseAiQuestion>