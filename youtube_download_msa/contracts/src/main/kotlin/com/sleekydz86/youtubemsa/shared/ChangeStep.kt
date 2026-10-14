package com.sleekydz86.youtubemsa.shared

enum class ChangeStep { FULFILL, PROJECT }

fun changeStep(status: DownloadStatus): ChangeStep =
    if (status == DownloadStatus.REQUESTED) ChangeStep.FULFILL else ChangeStep.PROJECT
