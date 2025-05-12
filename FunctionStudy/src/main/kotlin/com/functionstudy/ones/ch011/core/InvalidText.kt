package com.functionstudy.ones.ch011.core

data class InvalidText(val text: String) : DomainError("유효하지 않은 텍스트입니다: $text")