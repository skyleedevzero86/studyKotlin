package com.chaorks.domain.repository

import com.chaorks.domain.entity.AIChatRoom
import org.springframework.data.jpa.repository.JpaRepository

interface AiChatRoomRepository : JpaRepository<AIChatRoom, Long>