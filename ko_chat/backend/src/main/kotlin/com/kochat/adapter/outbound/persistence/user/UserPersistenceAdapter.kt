package com.kochat.adapter.outbound.persistence.user

import com.kochat.domain.user.model.User
import com.kochat.domain.user.port.out.UserPersistencePort
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UserPersistenceAdapter(
    private val userJpaRepository: UserJpaRepository,
) : UserPersistencePort {

    @Transactional
    override fun save(user: User): User {
        val saved = userJpaRepository.save(UserPersistenceMapper.toEntity(user))
        return UserPersistenceMapper.toDomain(saved)
    }

    @Transactional(readOnly = true)
    override fun findByUsername(username: String): User? =
        userJpaRepository.findByUsername(username)?.let(UserPersistenceMapper::toDomain)

    @Transactional(readOnly = true)
    override fun findById(id: Long): User? =
        userJpaRepository.findById(id).orElse(null)?.let(UserPersistenceMapper::toDomain)

    @Transactional(readOnly = true)
    override fun findAll(): List<User> =
        userJpaRepository.findAll().map(UserPersistenceMapper::toDomain)

    @Transactional(readOnly = true)
    override fun findAll(pageable: Pageable): Page<User> =
        userJpaRepository.findAll(pageable).map(UserPersistenceMapper::toDomain)

    @Transactional(readOnly = true)
    override fun existsByUsername(username: String): Boolean =
        userJpaRepository.existsByUsername(username)

    @Transactional
    override fun deleteByUsername(username: String) {
        userJpaRepository.deleteByUsername(username)
    }
}
