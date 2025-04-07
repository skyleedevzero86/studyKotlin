package com.chaorks.repository

import com.chaorks.domain.AIChatRoomSummaryMessage
import org.springframework.data.jpa.repository.JpaRepository

interface AIChatRoomSummaryMessageRepository : JpaRepository<AIChatRoomSummaryMessage, Long>