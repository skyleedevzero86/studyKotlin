package com.kominioai.domain.auth.domain.service

import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.UUID

@Service
class TwoFactorAuthService {
    private val secureRandom = SecureRandom()

    fun generateOTP(): String {
        return String.format("%06d", secureRandom.nextInt(1000000))
    }

    fun generateBackupCodes(count: Int = 10): List<String> {
        return (1..count).map { generateBackupCode() }
    }

    private fun generateBackupCode(): String {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).uppercase()
    }

    fun validateOTP(inputOTP: String, expectedOTP: String): Boolean {
        return inputOTP == expectedOTP
    }

    fun validateBackupCode(inputCode: String, backupCodes: List<String>): Boolean {
        return backupCodes.contains(inputCode.uppercase())
    }

    fun generateQRCodeUrl(username: String, secret: String, issuer: String): String {
        val encodedUsername = java.net.URLEncoder.encode(username, "UTF-8")
        val encodedSecret = java.net.URLEncoder.encode(secret, "UTF-8")
        val encodedIssuer = java.net.URLEncoder.encode(issuer, "UTF-8")

        return "otpauth://totp/$encodedIssuer:$encodedUsername?secret=$encodedSecret&issuer=$encodedIssuer"
    }
}