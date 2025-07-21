package com.kominioai.adapter.out.persistence

import com.kominioai.domain.auth.domain.model.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.r2dbc.core.DatabaseClient
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime

class UserPersistenceAdapterTest : StringSpec({
    val client = mockk<DatabaseClient>()
    val adapter = UserPersistenceAdapter(client)

    "loadById should return user when found" {
        val user = User.create(
            email = Email("test@example.com"),
            passwordHash = PasswordHash("hashed"),
            username = Username("testuser")
        )

        val mockRow = mockk<io.r2dbc.spi.Row>()
        every { mockRow.get("id", String::class.java) } returns user.id.value
        every { mockRow.get("email", String::class.java) } returns user.email.value
        every { mockRow.get("password_hash", String::class.java) } returns user.passwordHash.value
        every { mockRow.get("username", String::class.java) } returns user.username.value
        every { mockRow.get("account_status", String::class.java) } returns "ACTIVE"
        every { mockRow.get("email_verified", java.lang.Boolean::class.java) } returns false
        every { mockRow.get("two_factor_enabled", java.lang.Boolean::class.java) } returns false
        every { mockRow.get("failed_login_attempts", Integer::class.java) } returns 0
        every { mockRow.get("created_at", LocalDateTime::class.java) } returns LocalDateTime.now()
        every { mockRow.get("updated_at", LocalDateTime::class.java) } returns LocalDateTime.now()

        val mockSpec = mockk<DatabaseClient.GenericExecuteSpec>()
        every { client.sql(any()) } returns mockSpec
        every { mockSpec.bind(any(), any()) } returns mockSpec
        every { mockSpec.map(any<kotlin.Function2<io.r2dbc.spi.Row, io.r2dbc.spi.RowMetadata, User>>()) } returns mockSpec
        every { mockSpec.one() } returns Mono.just(user)

        StepVerifier.create(adapter.loadById(UserId("user-id")))
            .expectNextMatches { foundUser ->
                foundUser?.email?.value == "test@example.com" &&
                        foundUser?.username?.value == "testuser"
            }
            .verifyComplete()
    }

    "loadById should return empty when user not found" {
        val mockSpec = mockk<DatabaseClient.GenericExecuteSpec>()
        every { client.sql(any()) } returns mockSpec
        every { mockSpec.bind(any(), any()) } returns mockSpec
        every { mockSpec.map(any<kotlin.Function2<io.r2dbc.spi.Row, io.r2dbc.spi.RowMetadata, User>>()) } returns mockSpec
        every { mockSpec.one() } returns Mono.empty()

        StepVerifier.create(adapter.loadById(UserId("nonexistent")))
            .expectNextCount(0)
            .verifyComplete()
    }
})