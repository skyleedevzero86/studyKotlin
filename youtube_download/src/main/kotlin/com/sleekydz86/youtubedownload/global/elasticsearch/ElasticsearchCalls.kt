package com.sleekydz86.youtubedownload.global.elasticsearch

import com.sleekydz86.youtubedownload.global.env.StudyProperties
import com.sleekydz86.youtubedownload.global.error.DownloadException
import org.springframework.http.HttpStatus

internal fun vectorFailure(elasticsearch: StudyProperties.Elasticsearch, action: String, cause: Exception) =
    DownloadException(
        "Elasticsearch $action 에 실패했습니다. url=${elasticsearch.url} index=${elasticsearch.index}",
        cause,
        HttpStatus.BAD_GATEWAY,
    )
