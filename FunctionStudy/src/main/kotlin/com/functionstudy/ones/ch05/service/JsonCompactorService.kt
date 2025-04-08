package com.functionstudy.ones.ch05.service

import com.functionstudy.ch05.domain.JsonCompactor
import com.functionstudy.ch05.domain.OutQuotes


fun compactJson(json: String): String =
    json.fold(OutQuotes("") as JsonCompactor) { prev, c -> prev.compact(c) }.jsonCompacted
