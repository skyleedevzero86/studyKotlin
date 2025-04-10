package com.kpaypal.global.config

import com.paypal.base.rest.APIContext
import com.paypal.base.rest.OAuthTokenCredential
import com.paypal.base.rest.PayPalRESTException
import com.paypal.api.payments.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.*

@Configuration
class PayPalConfig(
    @Value("\${paypal.client.id}") private val clientId: String,
    @Value("\${paypal.client.secret}") private val clientSecret: String,
    @Value("\${paypal.mode}") private val mode: String
) {

    @Bean
    fun payPalSDKConfig(): Map<String, String> = mapOf("mode" to mode)

    @Bean
    fun oAuthTokenCredential(): OAuthTokenCredential =
        OAuthTokenCredential(clientId, clientSecret, payPalSDKConfig())

    @Bean
    @Throws(PayPalRESTException::class)
    fun apiContext(): APIContext =
        APIContext(oAuthTokenCredential().accessToken).apply {
            configurationMap = payPalSDKConfig()
        }
}