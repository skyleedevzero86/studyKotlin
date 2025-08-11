package com.sleekydz86.enums

enum class SSEMsgType(
    val type: String,
    val value: String
) {
    MESSAGE("message", "단일 전송 일반 메시지"),
    ADD("add", "메시지 추가, 스트리밍 stream 푸시에 적용"),
    FINISH("finish", "메시지 전송 완료"),
    CUSTOM_EVENT("custom_event", "사용자 정의 메시지 타입"),
    DONE("done", "메시지 전송 완료")
}