package com.sleekydz86.cdcmigration.global.error

import org.springframework.http.HttpStatus

enum class ErrorCode(val status: HttpStatus, val code: String, val message: String) {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다."),
    CSV_FORMAT(HttpStatus.BAD_REQUEST, "C002", "CSV 형식이 올바르지 않습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "C404", "요청한 주소를 찾을 수 없습니다."),
    MIGRATION_RUN_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "이관 작업을 찾을 수 없습니다."),
    MIGRATION_ALREADY_RUNNING(HttpStatus.CONFLICT, "M002", "이미 실행 중인 이관 작업입니다."),
    MIGRATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "M003", "이관 실행에 실패했습니다. 작업 상세에서 원인을 확인하고 재처리하세요."),
    RESET_BLOCKED(HttpStatus.CONFLICT, "M004", "지금은 대상 데이터를 초기화할 수 없습니다."),
    SUBMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "심의 신청을 찾을 수 없습니다."),
    ATTACHMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "A001", "첨부파일을 찾을 수 없습니다."),
    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "A002", "파일이 업로드 상한을 넘었습니다."),
    FILE_EMPTY(HttpStatus.BAD_REQUEST, "A003", "빈 파일은 올릴 수 없습니다."),
    REQUEST_ID_REQUIRED(HttpStatus.BAD_REQUEST, "A004", "X-Request-Id 헤더가 필요합니다."),
    REQUEST_ID_CONFLICT(HttpStatus.CONFLICT, "A005", "같은 요청 ID로 다른 파일이 들어왔습니다."),
    STORAGE_FAILED(HttpStatus.BAD_GATEWAY, "A006", "파일 저장소(MinIO) 처리에 실패했습니다."),
    CONTENT_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "A007", "DB에 파일 본문을 저장하지 못했습니다. 같은 요청 ID로 다시 보내면 복구됩니다."),
    ATTACHMENT_STATE(HttpStatus.CONFLICT, "A008", "첨부파일 상태를 바꿀 수 없습니다."),
    CDC_ALREADY_RUNNING(HttpStatus.CONFLICT, "D001", "이미 진행 중인 CDC 실습이 있습니다."),
    CDC_RUN_NOT_FOUND(HttpStatus.NOT_FOUND, "D002", "CDC 실행 기록을 찾을 수 없습니다."),
    CONNECT_FAILED(HttpStatus.BAD_GATEWAY, "D003", "Debezium Connect 호출에 실패했습니다."),
    CDC_STATE(HttpStatus.CONFLICT, "D004", "지금 상태에서는 할 수 없는 CDC 작업입니다."),
    PROCEDURE_FAILED(HttpStatus.CONFLICT, "P001", "프로시저 처리에 실패했습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C500", "서버 내부 오류가 발생했습니다."),
}
