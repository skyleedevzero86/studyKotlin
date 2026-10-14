package com.sleekydz86.youtubemsa.download.api

import com.sleekydz86.youtubemsa.download.ledger.DownloadLedger
import com.sleekydz86.youtubemsa.shared.ClaimedView
import com.sleekydz86.youtubemsa.shared.DownloadView
import com.sleekydz86.youtubemsa.shared.MarkFailedBody
import com.sleekydz86.youtubemsa.shared.MarkSavedBody
import com.sleekydz86.youtubemsa.shared.Reaction
import com.sleekydz86.youtubemsa.shared.StudyException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "07-원장 내부")
@RestController
@RequestMapping("/internal/downloads")
class InternalDownloadController(private val ledger: DownloadLedger) {
    @GetMapping("/{id}")
    @Operation(summary = "원장 단건", description = "임베딩을 포함한 다운로드 행을 조회합니다.")
    fun find(
        @Parameter(name = "id", description = "다운로드 번호") @PathVariable id: Long,
    ): DownloadView = ledger.find(id) ?: throw StudyException("저장된 영상을 찾지 못했습니다. 번호: $id", status = 404)

    @GetMapping
    @Operation(summary = "저장된 원장 목록", description = "상태가 SAVED인 행을 반환합니다.")
    fun saved(): List<DownloadView> = ledger.saved()

    @PostMapping("/{id}/claim")
    @Operation(summary = "저장 맡기", description = "이벤트 번호로 저장을 맡습니다. 이미 맡았으면 본문 없이 204입니다.")
    fun claim(
        @Parameter(name = "id", description = "다운로드 번호") @PathVariable id: Long,
        @RequestBody body: ClaimBody,
    ): ResponseEntity<ClaimedView> {
        val claimed = ledger.claim(id, body.eventId) ?: return ResponseEntity.noContent().build()
        return ResponseEntity.ok(claimed)
    }

    @PostMapping("/{id}/saved")
    @Operation(summary = "저장 완료", description = "파일 위치를 원장에 남깁니다. 반영하면 본문 없이 204입니다.")
    fun saved(
        @Parameter(name = "id", description = "다운로드 번호") @PathVariable id: Long,
        @RequestBody body: MarkSavedBody,
    ): ResponseEntity<Void> {
        ledger.markSaved(id, body)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/failed")
    @Operation(summary = "저장 실패", description = "실패 사유를 원장에 남깁니다. 반영하면 본문 없이 204입니다.")
    fun failed(
        @Parameter(name = "id", description = "다운로드 번호") @PathVariable id: Long,
        @RequestBody body: MarkFailedBody,
    ): ResponseEntity<Void> {
        ledger.markFailed(id, body.message)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/compensated")
    @Operation(summary = "보상 완료", description = "보상 처리 사유를 원장에 남깁니다. 반영하면 본문 없이 204입니다.")
    fun compensated(
        @Parameter(name = "id", description = "다운로드 번호") @PathVariable id: Long,
        @RequestBody body: MarkFailedBody,
    ): ResponseEntity<Void> {
        ledger.markCompensated(id, body.message)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/reactions")
    @Operation(summary = "반응 반영", description = "reaction은 LIKE, DISLIKE, VIEW 중 하나입니다.")
    fun react(
        @Parameter(name = "id", description = "다운로드 번호") @PathVariable id: Long,
        @RequestBody body: ReactionBody,
    ): DownloadView =
        ledger.react(id, reactionOf(body.reaction))
}

private fun reactionOf(raw: String): Reaction =
    Reaction.entries.firstOrNull { it.name == raw }
        ?: throw StudyException("알 수 없는 반응입니다. 입력값: $raw")
