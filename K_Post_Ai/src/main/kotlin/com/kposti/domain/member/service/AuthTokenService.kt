package com.kposti.domain.member.service

import com.kposti.domain.member.entity.Member
import com.kposti.standard.utils.UtClass
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AuthTokenService {

    @Value("\${custom.jwt.secretKey}")
    private lateinit var jwtSecretKey: String

    @Value("\${custom.accessToken.expirationSeconds}")
    private var accessTokenExpirationSeconds: Long = 0

    fun genAccessToken(member: Member): String =
        UtClass.jwt.toString(
            jwtSecretKey,
            accessTokenExpirationSeconds,
            mapOf(
                "id" to member.id,
                "username" to member.username,
                "nickname" to member.nickname,
                "authorities" to member.getAuthoritiesAsStringList()
            )
        )

    fun payload(accessToken: String): Map<String, Any>? =
        UtClass.jwt.payload(jwtSecretKey, accessToken)?.let { parsed ->
            mapOf(
                "id" to (parsed["id"] as Number).toLong(), // 타입 안정성 확보
                "username" to (parsed["username"] as? String).orEmpty(),
                "nickname" to (parsed["nickname"] as? String).orEmpty(),
                "authorities" to (parsed["authorities"] as? List<*>)?.filterIsInstance<String>().orEmpty()
            )
        }
}