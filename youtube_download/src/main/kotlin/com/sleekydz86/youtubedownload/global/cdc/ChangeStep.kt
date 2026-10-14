package com.sleekydz86.youtubedownload.global.cdc

import com.sleekydz86.youtubedownload.video.DownloadStatus

enum class ChangeStep { FULFILL, PROJECT }

fun changeStep(status: DownloadStatus): ChangeStep =
    if (status == DownloadStatus.REQUESTED) ChangeStep.FULFILL else ChangeStep.PROJECT
