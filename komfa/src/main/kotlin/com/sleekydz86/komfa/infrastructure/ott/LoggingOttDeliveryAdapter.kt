package com.sleekydz86.komfa.infrastructure.ott

import com.sleekydz86.komfa.application.auth.OttDeliveryPort
import com.sleekydz86.komfa.domain.auth.OttDeliveryResult
import com.sleekydz86.komfa.domain.auth.TokenValue
import com.sleekydz86.komfa.domain.auth.Username
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
@Primary
@ConditionalOnProperty(name = ["komfa.mail.enabled"], havingValue = "false", matchIfMissing = true)
class LoggingOttDeliveryAdapter : OttDeliveryPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun deliver(username: Username, token: TokenValue): OttDeliveryResult {
        log.info("[OTT] username={} token={} (개발: 매직 링크는 로그에서 확인)", username.value, token.value)
        return OttDeliveryResult.Sent
    }
}