package com.sleekydz86.komfa.infrastructure.ott

import com.sleekydz86.komfa.domain.ott.OneTimeTokenEntity
import com.sleekydz86.komfa.infrastructure.persistence.OneTimeTokenRepository
import org.springframework.security.authentication.ott.DefaultOneTimeToken
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest
import org.springframework.security.authentication.ott.OneTimeToken
import org.springframework.security.authentication.ott.OneTimeTokenService
import org.springframework.security.authentication.ott.OneTimeTokenAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class JpaOneTimeTokenService(
    private val repository: OneTimeTokenRepository,
) : OneTimeTokenService {

    @Transactional
    override fun generate(request: GenerateOneTimeTokenRequest): OneTimeToken {
        val tokenValue = UUID.randomUUID().toString()
        val expiresAt = Instant.now().plus(request.expiresIn)
        val entity = OneTimeTokenEntity(
            tokenValue = tokenValue,
            username = request.username,
            expiresAt = expiresAt,
        )
        repository.save(entity)
        return DefaultOneTimeToken(tokenValue, request.username, expiresAt)
    }

    @Transactional
    override fun consume(authenticationToken: OneTimeTokenAuthenticationToken): OneTimeToken? {
        val tokenValue = authenticationToken.tokenValue ?: return null
        val entity = repository.findByTokenValue(tokenValue) ?: return null
        if (entity.expiresAt.isBefore(Instant.now())) {
            repository.delete(entity)
            return null
        }
        repository.delete(entity)
        return DefaultOneTimeToken(entity.tokenValue, entity.username, entity.expiresAt)
    }
}
