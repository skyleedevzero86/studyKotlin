package com.kposti.global.app

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.tika.Tika
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.io.ClassPathResource

@Configuration
class AppConfig {

    companion object {
        private lateinit var environment: Environment

        fun isProd(): Boolean = environment.matchesProfiles("prod")
        fun isDev(): Boolean = environment.matchesProfiles("dev")
        fun isTest(): Boolean = environment.matchesProfiles("test")
        fun isNotProd(): Boolean = !isProd()

        lateinit var objectMapper: ObjectMapper
            private set

        lateinit var tika: Tika
            private set

        lateinit var siteFrontUrl: String
            private set

        lateinit var siteBackUrl: String
            private set

        lateinit var genFileDirPath: String
            private set

        lateinit var springServletMultipartMaxFileSize: String
            private set

        lateinit var springServletMultipartMaxRequestSize: String
            private set

        fun getTempDirPath(): String = System.getProperty("java.io.tmpdir")

        private var resourcesSampleDirPath: String? = null

        fun getResourcesSampleDirPath(): String {
            return resourcesSampleDirPath ?: run {
                val resource = ClassPathResource("sample")
                resource.file.absolutePath.also {
                    resourcesSampleDirPath = it
                }
            }
        }

    }

    @Autowired
    fun setEnvironment(environment: Environment) {
        Companion.environment = environment
    }

    @Autowired
    fun setObjectMapper(objectMapper: ObjectMapper) {
        Companion.objectMapper = objectMapper
    }

    @Autowired
    fun setTika(tika: Tika) {
        Companion.tika = tika
    }

    @Value("\${custom.site.frontUrl}")
    fun setSiteFrontUrl(siteFrontUrl: String) {
        Companion.siteFrontUrl = siteFrontUrl
    }

    @Value("\${custom.site.backUrl}")
    fun setSiteBackUrl(siteBackUrl: String) {
        Companion.siteBackUrl = siteBackUrl
    }

    @Value("\${custom.genFile.dirPath}")
    fun setGenFileDirPath(genFileDirPath: String) {
        Companion.genFileDirPath = genFileDirPath
    }

    @Value("\${spring.servlet.multipart.max-file-size}")
    fun setSpringServletMultipartMaxFileSize(springServletMultipartMaxFileSize: String) {
        Companion.springServletMultipartMaxFileSize = springServletMultipartMaxFileSize
    }

    @Value("\${spring.servlet.multipart.max-request-size}")
    fun setSpringServletMultipartMaxRequestSize(springServletMultipartMaxRequestSize: String) {
        Companion.springServletMultipartMaxRequestSize = springServletMultipartMaxRequestSize
    }
}