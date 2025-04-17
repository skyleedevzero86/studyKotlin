package com.chaorks.domain.repository

import com.chaorks.domain.entity.AIChatRoomSummaryMessage
import org.springframework.data.jpa.repository.JpaRepository

interface AIChatRoomSummaryMessageRepository : JpaRepository<AIChatRoomSummaryMessage, Long>