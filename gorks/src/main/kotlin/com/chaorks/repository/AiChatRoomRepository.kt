package com.chaorks.repository

import com.chaorks.domain.AIChatRoom
import org.springframework.data.jpa.repository.JpaRepository

interface AiChatRoomRepository : JpaRepository<AIChatRoom, Long>