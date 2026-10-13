package com.sleekydz86.densevector.global.persistence

import com.sleekydz86.densevector.global.config.VectorProperties
import com.sleekydz86.densevector.global.error.VectorStudyException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class PassageDimGuard(
    private val repository: PassageRepository,
    private val properties: VectorProperties
) {
    fun assertMatchesConfig() {
        val stored = repository.findFirstByOrderByIdAsc()?.dims ?: return
        if (stored == properties.dims) {
            return
        }
        throw VectorStudyException(
            HttpStatus.CONFLICT,
            "저장된 문단 dims가 설정과 다릅니다. 저장값=$stored, 설정=${properties.dims}. passage 테이블을 비운 뒤 다시 실행하세요."
        )
    }
}
