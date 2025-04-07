package com.kstomp.global

interface StompMessageTemplate {
    fun convertAndSend(type: String, destination: String, payload: Any)
}