package com.kominioai.domain.bulletin

import com.kominioai.domain.bulletin.application.dto.CreatePostRequest
import com.kominioai.domain.bulletin.application.dto.UpdatePostRequest
import com.kominioai.domain.bulletin.application.port.`in`.PostUseCase
import com.kominioai.domain.bulletin.application.port.out.PostPersistencePort
import com.kominioai.domain.bulletin.application.port.out.EventPublisherPort
import com.kominioai.domain.bulletin.application.service.PostApplicationService
import com.kominioai.domain.bulletin.domain.model.PostCategory
import com.kominioai.domain.bulletin.domain.service.PostService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@ExtendWith(MockitoExtension::class)
class PostApplicationServiceTest {

    @Mock
    private lateinit var postPersistencePort: PostPersistencePort

    @Mock
    private lateinit var eventPublisherPort: EventPublisherPort

    @Mock
    private lateinit var postService: PostService

    private lateinit var postApplicationService: PostApplicationService

    @Test
    fun `createPost should create post successfully`() {
        // Given
        val request = CreatePostRequest(
            title = "Test Post",
            content = "Test Content",
            category = PostCategory.COMMUNITY,
            pinned = false
        )
        val authorId = "user-1"
        val authorName = "Test User"

        `when`(postService.validatePostCreation(any(), any(), any(), any())).thenReturn(true)
        `when`(postService.shouldPinPost(any(), any())).thenReturn(false)
        `when`(postPersistencePort.save(any())).thenReturn(Mono.just(createMockPost()))
        `when`(eventPublisherPort.publishPostCreatedEvent(any())).thenReturn(Mono.empty())

        postApplicationService = PostApplicationService(postPersistencePort, eventPublisherPort, postService)

        // When
        val result = postApplicationService.createPost(request, authorId, authorName)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { response ->
                response.title == "Test Post" &&
                response.content == "Test Content" &&
                response.category == PostCategory.COMMUNITY
            }
            .verifyComplete()

        verify(postPersistencePort).save(any())
        verify(eventPublisherPort).publishPostCreatedEvent(any())
    }

    @Test
    fun `createPost should fail when validation fails`() {
        // Given
        val request = CreatePostRequest(
            title = "",
            content = "Test Content",
            category = PostCategory.COMMUNITY,
            pinned = false
        )
        val authorId = "user-1"
        val authorName = "Test User"

        `when`(postService.validatePostCreation(any(), any(), any(), any())).thenReturn(false)

        postApplicationService = PostApplicationService(postPersistencePort, eventPublisherPort, postService)

        // When
        val result = postApplicationService.createPost(request, authorId, authorName)

        // Then
        StepVerifier.create(result)
            .expectError(IllegalArgumentException::class.java)
            .verify()
    }

    private fun createMockPost() = com.kominioai.domain.bulletin.domain.model.Post(
        id = com.kominioai.domain.bulletin.domain.model.PostId("post-1"),
        title = "Test Post",
        content = "Test Content",
        category = PostCategory.COMMUNITY,
        authorId = "user-1",
        authorName = "Test User",
        pinned = false,
        viewCount = 0,
        likeCount = 0,
        commentCount = 0,
        createdAt = java.time.LocalDateTime.now(),
        updatedAt = java.time.LocalDateTime.now()
    )
}
