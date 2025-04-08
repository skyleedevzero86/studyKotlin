package com.functionstudy.ones.ch03.utils

data class StringTag(val text: String)
fun tag(value: String): StringTag = StringTag(value)