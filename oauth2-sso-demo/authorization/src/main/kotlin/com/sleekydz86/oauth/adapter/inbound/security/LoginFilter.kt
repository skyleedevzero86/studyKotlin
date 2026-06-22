package com.sleekydz86.oauth.adapter.inbound.security

import com.sleekydz86.oauth.adapter.inbound.web.user.dto.LoginUserRequest
import com.sleekydz86.oauth.domain.user.exception.LoginDeniedException
import com.sleekydz86.oauth.domain.user.exception.PasswordChangeRequiredException
import com.sleekydz86.oauth.global.exception.ApiErrorResponse
import com.sleekydz86.oauth.global.exception.ErrorCode
import com.sleekydz86.oauth.global.exception.ErrorResponseWriter
import com.sleekydz86.oauth.global.security.login.LoginAccountValidator
import com.sleekydz86.oauth.global.security.login.LoginAuthenticationFailureHandler
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.util.StreamUtils
import tools.jackson.databind.ObjectMapper
import java.nio.charset.StandardCharsets

class LoginFilter(
    authenticationManager: AuthenticationManager,
    private val authenticationSuccessHandler: AuthenticationSuccessHandler,
    private val authenticationFailureHandler: AuthenticationFailureHandler,
    private val loginAccountValidator: LoginAccountValidator,
    private val errorResponseWriter: ErrorResponseWriter,
    private val objectMapper: ObjectMapper,
) : AbstractAuthenticationProcessingFilter(DEFAULT_REQUEST_MATCHER, authenticationManager) {

    init {
        setAuthenticationSuccessHandler(authenticationSuccessHandler)
        setAuthenticationFailureHandler(authenticationFailureHandler)
    }

    override fun attemptAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Authentication {
        if (request.method != HttpMethod.POST.name()) {
            throw AuthenticationServiceException(
                "${ErrorCode.UNSUPPORTED_HTTP_METHOD.defaultMessage}: ${request.method}",
            )
        }

        return try {
            val messageBody = StreamUtils.copyToString(request.inputStream, StandardCharsets.UTF_8)
            val loginRequest = objectMapper.readValue(messageBody, LoginUserRequest::class.java)

            request.setAttribute(LoginAuthenticationFailureHandler.LOGIN_USERNAME_ATTRIBUTE, loginRequest.username)

            val authRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                loginRequest.username,
                loginRequest.password,
            )
            setDetails(request, authRequest)

            authenticationManager.authenticate(authRequest)
        } catch (ex: AuthenticationException) {
            throw ex
        } catch (ex: Exception) {
            throw AuthenticationServiceException(ErrorCode.INVALID_REQUEST_BODY.defaultMessage, ex)
        }
    }

    override fun successfulAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
        authResult: Authentication,
    ) {
        try {
            loginAccountValidator.validate(authResult.name ?: return)
            authenticationSuccessHandler.onAuthenticationSuccess(request, response, authResult)
        } catch (ex: PasswordChangeRequiredException) {
            writeError(
                response,
                HttpStatus.FORBIDDEN.value(),
                ex.message ?: ErrorCode.PASSWORD_CHANGE_REQUIRED.defaultMessage,
                ErrorCode.PASSWORD_CHANGE_REQUIRED.name,
            )
        } catch (ex: LoginDeniedException) {
            writeError(
                response,
                HttpStatus.FORBIDDEN.value(),
                ex.message ?: ErrorCode.LOGIN_DENIED.defaultMessage,
                ErrorCode.LOGIN_DENIED.name,
            )
        }
    }

    private fun setDetails(request: HttpServletRequest, authRequest: UsernamePasswordAuthenticationToken) {
        authRequest.details = authenticationDetailsSource.buildDetails(request)
    }

    private fun writeError(response: HttpServletResponse, status: Int, message: String, code: String) {
        errorResponseWriter.write(
            response,
            status,
            ApiErrorResponse(error = message, code = code),
        )
    }

    companion object {
        private val DEFAULT_REQUEST_MATCHER: RequestMatcher =
            PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/api/v1/login")
    }
}
