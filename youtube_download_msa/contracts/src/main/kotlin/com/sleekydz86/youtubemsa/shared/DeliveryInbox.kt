package com.sleekydz86.youtubemsa.shared

interface DeliveryInbox {
    fun alreadyDone(consumer: String, eventId: String): Boolean
    fun markDone(consumer: String, eventId: String)
}

fun requireEventId(raw: String): String {
    val id = raw.trim()
    if (id.isEmpty() || id.length > 80) {
        throw StudyException("이벤트 ID는 1자 이상 80자 이하여야 합니다. 입력값: $raw")
    }
    return id
}

fun requireConsumer(raw: String): String {
    val name = raw.trim()
    if (name !in setOf("media", "search", "dlq")) throw StudyException("알 수 없는 소비자입니다. 입력값: $raw")
    return name
}

fun acceptDelivery(inbox: DeliveryInbox, consumer: String, eventId: String, action: () -> Unit): Boolean {
    val name = requireConsumer(consumer)
    val id = requireEventId(eventId)
    if (inbox.alreadyDone(name, id)) return false
    action()
    inbox.markDone(name, id)
    return true
}
