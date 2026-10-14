package com.sleekydz86.youtubemsa.search

import com.sleekydz86.youtubemsa.shared.DownloadView

interface DownloadLedgerClient {
    fun find(id: Long): DownloadView?
    fun saved(): List<DownloadView>
    fun react(id: Long, reaction: String): DownloadView
}
