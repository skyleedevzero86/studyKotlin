package com.kochat.global.exception

enum class ErrorCode(val defaultMessage: String) {
    AUTHENTICATION_FAILED("아이디 또는 비밀번호가 올바르지 않습니다."),
    ACCESS_DENIED("접근 권한이 없습니다."),
    LOGIN_DENIED("로그인이 거부되었습니다."),
    PASSWORD_CHANGE_REQUIRED("비밀번호 변경 기간(30일)이 지났습니다. 비밀번호를 변경해주세요."),
    INVALID_TOKEN("유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN("토큰이 만료되었거나 유효하지 않습니다."),
    INACTIVE_ACCOUNT("이용할 수 없는 계정입니다. 관리자에게 문의하세요."),
    PASSWORD_EXPIRED("비밀번호 변경 기간(30일)이 지났습니다."),
    DUPLICATE_USERNAME("이미 사용 중인 아이디입니다."),
    USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
    INVALID_USER_STATUS("현재 회원 상태에서는 요청을 처리할 수 없습니다."),
    INVALID_CURRENT_PASSWORD("현재 비밀번호가 일치하지 않습니다."),
    PASSWORD_CHANGE_LOCKED("비밀번호 변경 실패 횟수(3회)를 초과했습니다. 관리자에게 문의하세요."),
    VALIDATION_FAILED("입력값이 올바르지 않습니다."),
    INVALID_REQUEST_BODY("요청 본문 형식이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    UNSUPPORTED_HTTP_METHOD("지원하지 않는 HTTP 메서드입니다."),
}
