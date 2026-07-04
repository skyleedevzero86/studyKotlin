package com.kochat.global.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.type.CollectionType
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class PageJacksonModule : SimpleModule() {
    init {
        addDeserializer(Page::class.java, PageDeserializer())
    }
}

private class PageDeserializer : JsonDeserializer<Page<*>>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Page<*> {
        val node = p.codec.readTree<JsonNode>(p)
        val content = node.get("content")
        val number = node.get("number")?.asInt() ?: 0
        val size = node.get("size")?.asInt() ?: 20
        val totalElements = node.get("totalElements")?.asLong() ?: 0L

        val mapper = p.codec as com.fasterxml.jackson.databind.ObjectMapper
        val listType: CollectionType = ctxt.typeFactory.constructCollectionType(
            ArrayList::class.java,
            Any::class.java,
        )
        val items: List<Any> = if (content != null && content.isArray) {
            mapper.readValue(mapper.treeAsTokens(content), listType) ?: emptyList()
        } else {
            emptyList()
        }

        return PageImpl(items, PageRequest.of(number, maxOf(size, 1)), totalElements)
    }
}
