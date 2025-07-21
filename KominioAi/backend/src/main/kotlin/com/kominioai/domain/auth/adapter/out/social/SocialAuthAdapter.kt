package com.kominioai.domain.auth.adapter.out.social

import com.kominioai.domain.auth.application.dto.SocialUserInfo
import com.kominioai.domain.auth.application.port.out.SocialAuthPort
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class SocialAuthAdapter : SocialAuthPort {
    override fun getGoogleUserInfo(accessToken: String): Mono<SocialUserInfo> {
        // 실제 Google API 호출 구현 필요
        return Mono.just(SocialUserInfo("GOOGLE", "google-id", "user@gmail.com", "Google User", null, null, null))
    }
    override fun getKakaoUserInfo(accessToken: String): Mono<SocialUserInfo> {
        // 실제 Kakao API 호출 구현 필요
        return Mono.just(SocialUserInfo("KAKAO", "kakao-id", "user@kakao.com", "Kakao User", null, null, null))
    }
    override fun getNaverUserInfo(accessToken: String): Mono<SocialUserInfo> {
        // 실제 Naver API 호출 구현 필요
        return Mono.just(SocialUserInfo("NAVER", "naver-id", "user@naver.com", "Naver User", null, null, null))
    }
    override fun validateSocialToken(provider: String, accessToken: String): Mono<Boolean> {
        // 실제 토큰 검증 구현 필요
        return Mono.just(true)
    }
}