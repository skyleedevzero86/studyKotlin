package com.sleekydz86.cdcmigration.migration.application

import com.sleekydz86.cdcmigration.global.error.BusinessException
import com.sleekydz86.cdcmigration.global.error.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TargetResetService(
    private val runs: MigrationRunRepository,
    private val cdcRuns: CdcRunRepository,
    private val cleaner: TargetDataCleaner,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun reset() {
        if (cdcRuns.findActive().isNotEmpty()) {
            throw BusinessException(ErrorCode.RESET_BLOCKED, "CDC 실습이 진행 중입니다. CDC 반영 현황에서 먼저 중지하세요.")
        }
        if (runs.anyRunning()) {
            throw BusinessException(ErrorCode.RESET_BLOCKED, "실행 중인 이관 작업이 있습니다.")
        }
        cleaner.clean()
        log.info("대상 데이터(target_db)를 초기화했습니다. 코드 매핑과 가상 과제 기준은 유지합니다.")
    }
}
