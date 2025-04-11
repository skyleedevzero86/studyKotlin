package com.kposti.global.security

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Component

@Component
class CustomAuthorizationRequestResolver(
    clientRegistrationRepository: ClientRegistrationRepository
) : OAuth2AuthorizationRequestResolver {

    private val defaultResolver = DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization")

    override fun resolve(request: HttpServletRequest?): OAuth2AuthorizationRequest? =
        customize(defaultResolver.resolve(request), request)

    override fun resolve(request: HttpServletRequest?, clientRegistrationId: String?): OAuth2AuthorizationRequest? =
        customize(defaultResolver.resolve(request, clientRegistrationId), request)

    private fun customize(authRequest: OAuth2AuthorizationRequest?, request: HttpServletRequest?): OAuth2AuthorizationRequest? {
        if (authRequest == null || request == null) return null

        val redirectUrl = request.getParameter("redirectUrl") ?: return authRequest
        val updatedParams = authRequest.additionalParameters.toMutableMap().apply {
            put("state", redirectUrl)
        }

        return OAuth2AuthorizationRequest.from(authRequest)
            .additionalParameters(updatedParams)
            .state(redirectUrl)
            .build()
    }
}