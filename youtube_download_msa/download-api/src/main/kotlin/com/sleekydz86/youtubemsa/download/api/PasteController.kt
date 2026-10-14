package com.sleekydz86.youtubemsa.download.api

import com.sleekydz86.youtubemsa.download.ledger.DownloadLedger
import com.sleekydz86.youtubemsa.shared.DownloadTicket
import com.sleekydz86.youtubemsa.shared.PasteBody
import com.sleekydz86.youtubemsa.shared.sharedVideo
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/downloads")
class PasteController(private val ledger: DownloadLedger) {
    @PostMapping
    fun paste(@RequestBody body: PasteBody): DownloadTicket = ledger.request(sharedVideo(body.url))
}
