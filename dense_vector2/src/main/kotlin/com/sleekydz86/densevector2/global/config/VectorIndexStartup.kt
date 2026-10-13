package com.sleekydz86.densevector2.global.config

import com.sleekydz86.densevector2.global.elasticsearch.VectorIndexAdmin
import com.sleekydz86.densevector2.global.persistence.PassageDimGuard
import com.sleekydz86.densevector2.global.seed.PassageSeedService
import com.sleekydz86.densevector2.global.seed.SamplePassageLoader
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class VectorIndexStartup(
    private val vectorIndex: VectorIndexAdmin,
    private val dimGuard: PassageDimGuard,
    private val sampleLoader: SamplePassageLoader,
    private val seedService: PassageSeedService,
    private val properties: VectorProperties
) : ApplicationRunner {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        vectorIndex.ensureIndex()
        dimGuard.assertMatchesConfig()
        if (!properties.startupSeed) {
            log.info("기동 시 학습 문단 적재를 건너뜁니다.")
            return
        }
        val samples = sampleLoader.load()
        val seeded = seedService.fillTo(1000)
        log.info("학습용 문단 적재 완료 samples={} seeded={}", samples, seeded)
    }
}
