package com.koimg

import org.springframework.ai.vectorstore.chroma.autoconfigure.ChromaVectorStoreAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [ChromaVectorStoreAutoConfiguration::class])
class KoImgApplication

fun main(args: Array<String>) {
    runApplication<KoImgApplication>(*args)
}
