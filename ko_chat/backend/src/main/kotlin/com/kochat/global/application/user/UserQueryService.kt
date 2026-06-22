package com.kochat.global.application.user

import com.kochat.domain.user.port.out.UserPersistencePort
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserQueryService(
    private val userPersistencePort: UserPersistencePort,
) : UserDetailsService {

    @Transactional(readOnly = true)
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userPersistencePort.findByUsername(username)
            ?: throw UsernameNotFoundException("사용자를 찾을 수 없습니다: $username")

        return User.builder()
            .username(user.username)
            .password(user.password)
            .roles(user.role.name)
            .build()
    }
}
