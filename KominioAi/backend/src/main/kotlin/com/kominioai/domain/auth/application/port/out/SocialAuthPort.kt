package com.kominioai.domain.auth.application.port.out

import com.kominioai.domain.auth.application.dto.SocialUserInfo
import reactor.core.publisher.Mono

interface SocialAuthPort {
    fun getGoogleUserInfo(accessToken: String): Mono<SocialUserInfo>
    fun getKakaoUserInfo(accessToken: String): Mono<SocialUserInfo>
    fun getNaverUserInfo(accessToken: String): Mono<SocialUserInfo>
    fun validateSocialToken(provider: String, accessToken: String): Mono<Boolean>
}