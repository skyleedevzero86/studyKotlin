package com.kochat.global.crypto

import com.kochat.global.config.EncryptionProperties
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Service
class AesEncryptionService(
    encryptionProperties: EncryptionProperties,
) {
    private val secretKey = SecretKeySpec(deriveKey(encryptionProperties.secret), AES_ALGORITHM)
    private val secureRandom = SecureRandom()

    fun encrypt(plainText: String): String {
        val iv = ByteArray(GCM_IV_LENGTH)
        secureRandom.nextBytes(iv)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val encrypted = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))

        val combined = ByteArray(iv.size + encrypted.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)

        return Base64.getEncoder().encodeToString(combined)
    }

    fun decrypt(cipherText: String): String {
        val decoded = Base64.getDecoder().decode(cipherText)
        val iv = decoded.copyOfRange(0, GCM_IV_LENGTH)
        val encrypted = decoded.copyOfRange(GCM_IV_LENGTH, decoded.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val decrypted = cipher.doFinal(encrypted)
        return String(decrypted, StandardCharsets.UTF_8)
    }

    private fun deriveKey(secret: String): ByteArray =
        MessageDigest.getInstance("SHA-256")
            .digest(secret.toByteArray(StandardCharsets.UTF_8))

    companion object {
        private const val AES_ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
    }
}
