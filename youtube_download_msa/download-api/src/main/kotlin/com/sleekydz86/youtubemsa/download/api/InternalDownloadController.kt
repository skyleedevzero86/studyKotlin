package com.sleekydz86.youtubemsa.download.api

import com.sleekydz86.youtubemsa.download.ledger.DownloadLedger
import com.sleekydz86.youtubemsa.shared.ClaimedView
import com.sleekydz86.youtubemsa.shared.DownloadView
import com.sleekydz86.youtubemsa.shared.MarkFailedBody
import com.sleekydz86.youtubemsa.shared.MarkSavedBody
import com.sleekydz86.youtubemsa.shared.Reaction
import com.sleekydz86.youtubemsa.shared.StudyException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/downloads")
class InternalDownloadController(private val ledger: DownloadLedger) {
    @GetMapping("/{id}")
    fun find(@PathVariable id: Long): DownloadView =
        ledger.find(id) ?: throw StudyException("저장된 영상을 찾지 못했습니다. 번호: $id", status = 404)

    @GetMapping
    fun saved(): List<DownloadView> = ledger.saved()

    @PostMapping("/{id}/claim")
    fun claim(@PathVariable id: Long, @RequestBody body: ClaimBody): ResponseEntity<ClaimedView> {
        val claimed = ledger.claim(id, body.eventId) ?: return ResponseEntity.noContent().build()
        return ResponseEntity.ok(claimed)
    }

    @PostMapping("/{id}/saved")
    fun saved(@PathVariable id: Long, @RequestBody body: MarkSavedBody): ResponseEntity<Void> {
        ledger.markSaved(id, body)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/failed")
    fun failed(@PathVariable id: Long, @RequestBody body: MarkFailedBody): ResponseEntity<Void> {
        ledger.markFailed(id, body.message)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/compensated")
    fun compensated(@PathVariable id: Long, @RequestBody body: MarkFailedBody): ResponseEntity<Void> {
        ledger.markCompensated(id, body.message)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/reactions")
    fun react(@PathVariable id: Long, @RequestBody body: ReactionBody): DownloadView =
        ledger.react(id, reactionOf(body.reaction))
}

private fun reactionOf(raw: String): Reaction =
    Reaction.entries.firstOrNull { it.name == raw }
        ?: throw StudyException("알 수 없는 반응입니다. 입력값: $raw")
