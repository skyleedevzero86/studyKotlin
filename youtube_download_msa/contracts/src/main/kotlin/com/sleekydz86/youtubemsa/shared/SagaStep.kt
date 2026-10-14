package com.sleekydz86.youtubemsa.shared

enum class SagaStep { REQUESTED, FULFILLING, SAVED, FAILED, COMPENSATED }

fun nextSagaStep(current: SagaStep?, type: DownloadEventType): SagaStep =
    allowedStep(current, type) ?: throw StudyException(
        "사가 단계를 바꿀 수 없습니다. 현재: ${current?.name ?: "없음"}, 이벤트: ${type.name}",
        status = 409,
    )

fun shouldCompensate(uploaded: Boolean, saveFailed: Boolean): Boolean = uploaded && saveFailed

private fun allowedStep(current: SagaStep?, type: DownloadEventType): SagaStep? = when (type) {
    DownloadEventType.DOWNLOAD_REQUESTED -> when (current) {
        null, SagaStep.REQUESTED, SagaStep.FAILED, SagaStep.COMPENSATED, SagaStep.FULFILLING -> SagaStep.REQUESTED
        SagaStep.SAVED -> null
    }
    DownloadEventType.DOWNLOAD_CLAIMED -> when (current) {
        SagaStep.REQUESTED, SagaStep.FULFILLING -> SagaStep.FULFILLING
        else -> null
    }
    DownloadEventType.DOWNLOAD_SAVED -> if (current == SagaStep.FULFILLING) SagaStep.SAVED else null
    DownloadEventType.DOWNLOAD_FAILED -> if (current == SagaStep.FULFILLING) SagaStep.FAILED else null
    DownloadEventType.DOWNLOAD_COMPENSATED -> if (current == SagaStep.FULFILLING) SagaStep.COMPENSATED else null
    DownloadEventType.DOWNLOAD_REACTED -> if (current == SagaStep.SAVED) SagaStep.SAVED else null
}
