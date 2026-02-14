package com.sleekydz86.komfa.infrastructure.crypto


import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Service
class Aes256Service(
    @Value("\${komfa.encryption.key:}") private val keyBase64: String,
) {
    companion object {
        private const val ALG = "AES/GCM/NoPadding"
        private const val KEY_LEN = 32
        private const val IV_LEN = 12
        private const val TAG_LEN = 128
        private const val ENCRYPTED_PREFIX = "ENC:"
    }

    private val key: ByteArray? by lazy {
        if (keyBase64.isBlank()) return@lazy null
        val decoded = Base64.getDecoder().decode(keyBase64)
        if (decoded.size != KEY_LEN) return@lazy null
        decoded
    }

    fun encrypt(plain: String): String {
        if (plain.isBlank()) return plain
        val k = key ?: return plain
        val iv = ByteArray(IV_LEN).also { java.security.SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance(ALG)
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(k, "AES"), GCMParameterSpec(TAG_LEN, iv))
        val encrypted = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        val combined = iv + encrypted
        return ENCRYPTED_PREFIX + Base64.getEncoder().encodeToString(combined)
    }

    fun decrypt(cipherText: String): String? {
        if (cipherText.isBlank()) return cipherText
        if (!cipherText.startsWith(ENCRYPTED_PREFIX)) return null
        val k = key ?: return null
        return try {
            val decoded = Base64.getDecoder().decode(cipherText.removePrefix(ENCRYPTED_PREFIX))
            if (decoded.size < IV_LEN) return null
            val iv = decoded.copyOfRange(0, IV_LEN)
            val encrypted = decoded.copyOfRange(IV_LEN, decoded.size)
            val cipher = Cipher.getInstance(ALG)
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(k, "AES"), GCMParameterSpec(TAG_LEN, iv))
            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }

    fun isEncrypted(value: String?): Boolean = value != null && value.startsWith(ENCRYPTED_PREFIX)

    fun decryptOrPlain(value: String?): String? {
        if (value == null) return null
        if (value.isBlank()) return value
        return decrypt(value) ?: value
    }
}
