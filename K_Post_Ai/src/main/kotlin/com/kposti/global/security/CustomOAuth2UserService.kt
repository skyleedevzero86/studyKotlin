package com.kposti.global.security

import com.kposti.domain.member.service.MemberService
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import java.util.*
import org.springframework.transaction.annotation.Transactional

@Service
class CustomOAuth2UserService(
    private val memberService: MemberService
) : DefaultOAuth2UserService() {

    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        val oauthId = oAuth2User.name
        val provider = userRequest.clientRegistration.registrationId.uppercase(Locale.getDefault())

        val attributes = oAuth2User.attributes
        val properties = attributes["properties"] as? Map<*, *> ?: emptyMap<Any, Any>()
        val nickname = properties["nickname"] as? String ?: ""
        val profileImgUrl = properties["profile_image"] as? String ?: ""
        val username = "$provider__$oauthId"

        val member = memberService.modifyOrJoin(username, nickname, profileImgUrl)

        return SecurityUser(
            member.id,
            member.username,
            "",
            member.nickname,
            member.authorities
        )
    }
}