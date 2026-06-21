package com.sleekydz86.oauth.domain.user.exception

class PasswordChangeRequiredException :
    RuntimeException("비밀번호 변경 기간(30일)이 지났습니다. 비밀번호를 변경해주세요.")
