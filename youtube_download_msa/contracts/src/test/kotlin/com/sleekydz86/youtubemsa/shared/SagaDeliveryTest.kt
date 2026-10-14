package com.sleekydz86.youtubemsa.shared

import org.junit.jupiter.api.DisplayName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DisplayName("단위: 사가, Outbox, 재시도")
class SagaDeliveryTest {
    @Test
    @DisplayName("요청에서 저장 완료까지 사가 단계가 앞으로만 간다")
    fun sagaMovesForward() {
        // given
        val requested = nextSagaStep(null, DownloadEventType.DOWNLOAD_REQUESTED)

        // when
        val fulfilling = nextSagaStep(requested, DownloadEventType.DOWNLOAD_CLAIMED)
        val saved = nextSagaStep(fulfilling, DownloadEventType.DOWNLOAD_SAVED)

        // then
        assertEquals(SagaStep.REQUESTED, requested)
        assertEquals(SagaStep.FULFILLING, fulfilling)
        assertEquals(SagaStep.SAVED, saved)
    }

    @Test
    @DisplayName("올린 파일의 원장 반영이 실패하면 보상한다")
    fun compensatesAfterUpload() {
        // given
        val uploaded = true
        val saveFailed = true

        // when
        val compensate = shouldCompensate(uploaded, saveFailed)
        val step = nextSagaStep(SagaStep.FULFILLING, DownloadEventType.DOWNLOAD_COMPENSATED)

        // then
        assertTrue(compensate)
        assertFalse(shouldCompensate(uploaded = false, saveFailed = true))
        assertEquals(SagaStep.COMPENSATED, step)
    }

    @Test
    @DisplayName("저장된 사가에서 다시 저장을 시작하지 않는다")
    fun rejectsClaimAfterSaved() {
        // given
        val saved = SagaStep.SAVED

        // when
        val error = assertFailsWith<StudyException> { nextSagaStep(saved, DownloadEventType.DOWNLOAD_CLAIMED) }

        // then
        assertEquals(409, error.status)
    }

    @Test
    @DisplayName("같은 이벤트는 한 번만 처리하고 실패하면 다시 받는다")
    fun idempotentConsumer() {
        // given
        val inbox = MemoryInbox()
        var runs = 0

        // when
        val first = acceptDelivery(inbox, "media", "event-1") { runs += 1 }
        val error = assertFailsWith<IllegalStateException> {
            acceptDelivery(inbox, "search", "event-2") { error("검색 반영 실패") }
        }
        val retried = acceptDelivery(inbox, "search", "event-2") { runs += 1 }
        val duplicate = acceptDelivery(inbox, "media", "event-1") { runs += 1 }

        // then
        assertTrue(first)
        assertTrue(error.message!!.contains("검색 반영 실패"))
        assertTrue(retried)
        assertFalse(duplicate)
        assertEquals(2, runs)
    }

    @Test
    @DisplayName("다섯 번째 실패부터 DLQ로 보내고 대기 시간은 두 배씩 늘어난다")
    fun retriesThenDeadLetter() {
        // given
        val first = DeliveryPolicy.delayMillis(1)
        val second = DeliveryPolicy.delayMillis(2)

        // when
        val fifth = DeliveryPolicy.deadLetter(5)
        val fourth = DeliveryPolicy.deadLetter(4)

        // then
        assertEquals(1_000L, first)
        assertEquals(2_000L, second)
        assertEquals(16_000L, DeliveryPolicy.delayMillis(5))
        assertFalse(fourth)
        assertTrue(fifth)
    }

    @Test
    @DisplayName("Outbox 본문에서 이벤트 ID와 상태를 읽는다")
    fun readsOutboxPayload() {
        // given
        val payload = eventPayload("event-9", DownloadEventType.DOWNLOAD_REQUESTED, 4, "REQUESTED")

        // when
        val event = downloadEvent(payload)

        // then
        assertEquals("event-9", event?.eventId)
        assertEquals(DownloadEventType.DOWNLOAD_REQUESTED, event?.eventType)
        assertEquals(4L, event?.downloadId)
        assertNull(downloadEvent("""{"eventType":"UNKNOWN"}"""))
    }
}

private class MemoryInbox : DeliveryInbox {
    private val seen = mutableSetOf<String>()

    override fun alreadyDone(consumer: String, eventId: String): Boolean = "$consumer:$eventId" in seen

    override fun markDone(consumer: String, eventId: String) {
        seen += "$consumer:$eventId"
    }
}
