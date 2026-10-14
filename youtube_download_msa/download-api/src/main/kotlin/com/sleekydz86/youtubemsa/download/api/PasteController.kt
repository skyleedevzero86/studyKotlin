package com.sleekydz86.youtubemsa.download.api

import com.sleekydz86.youtubemsa.download.ledger.DownloadLedger
import com.sleekydz86.youtubemsa.shared.DownloadTicket
import com.sleekydz86.youtubemsa.shared.PasteBody
import com.sleekydz86.youtubemsa.shared.sharedVideo
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "01-다운로드 요청")
@RestController
@RequestMapping("/api/downloads")
class PasteController(private val ledger: DownloadLedger) {
    @PostMapping
    @Operation(
        summary = "영상 주소 등록",
        description = "유튜브 공유 주소를 원장에 남깁니다. watch, shorts, youtu.be 주소를 받습니다.",
    )
    fun paste(@RequestBody body: PasteBody): DownloadTicket = ledger.request(sharedVideo(body.url))
}
