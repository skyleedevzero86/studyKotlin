package com.karchitecture.auth.domain

import com.karchitecture.auth.domain.service.Encryptor
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Auth(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val username: String,
    val password: String,
) {

    fun matches(password: String, encryptor: Encryptor): Boolean {
        return encryptor.matches(password, this.password)
    }

    companion object {
        fun signUpWithEncryption(
            username: String,
            password: String,
            encryptor: Encryptor
        ) = Auth(username = username, password = encryptor.encrypt(password))
    }
}