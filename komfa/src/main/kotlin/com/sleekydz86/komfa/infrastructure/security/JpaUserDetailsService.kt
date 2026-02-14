package com.sleekydz86.komfa.infrastructure.security

import com.sleekydz86.komfa.domain.user.UserStatus
import com.sleekydz86.komfa.infrastructure.persistence.UserRepository
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class JpaUserDetailsService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val entity = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("사용자를 찾을 수 없습니다: $username")
        if (entity.status != UserStatus.ACTIVE.name) {
            val message = when (entity.status) {
                UserStatus.PENDING.name -> "승인 대기 중입니다. 관리자 승인 후 로그인할 수 있습니다."
                UserStatus.SUSPENDED.name -> "정지된 계정입니다."
                UserStatus.WITHDRAWN.name -> "탈퇴된 계정입니다."
                else -> "로그인할 수 없는 상태입니다."
            }
            throw BadCredentialsException(message)
        }
        val authorities = entity.roles.split(",").map { role ->
            SimpleGrantedAuthority(role.trim().let { if (it.startsWith("ROLE_")) it else "ROLE_$it" })
        }
        return User(
            entity.username,
            entity.passwordHash,
            true,
            true,
            true,
            true,
            authorities,
        )
    }
}
