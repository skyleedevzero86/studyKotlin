package com.kominioai.domain.bulletin

import com.kominioai.domain.bulletin.application.dto.CreateCommentRequest
import com.kominioai.domain.bulletin.application.dto.UpdateCommentRequest
import com.kominioai.domain.bulletin.application.port.`in`.CommentUseCase
import com.kominioai.domain.bulletin.application.port.out.CommentPersistencePort
import com.kominioai.domain.bulletin.application.port.out.EventPublisherPort
import com.kominioai.domain.bulletin.application.service.CommentApplicationService
import com.kominioai.domain.bulletin.domain.model.CommentId
import com.kominioai.domain.bulletin.domain.model.PostId
import com.kominioai.domain.bulletin.domain.service.CommentService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@ExtendWith(MockitoExtension::class)
class CommentApplicationServiceTest {

    @Mock
    private lateinit var commentPersistencePort: CommentPersistencePort

    @Mock
    private lateinit var eventPublisherPort: EventPublisherPort

    @Mock
    private lateinit var commentService: CommentService

    private lateinit var commentApplicationService: CommentApplicationService

    @Test
    fun `createComment should create comment successfully`() {
        // Given
        val request = CreateCommentRequest(
            content = "Test Comment",
            parentId = null
        )
        val postId = "post-1"
        val authorId = "user-1"
        val authorName = "Test User"

        `when`(commentService.validateCommentCreation(any(), any())).thenReturn(true)
        `when`(commentPersistencePort.save(any())).thenReturn(Mono.just(createMockComment()))
        `when`(eventPublisherPort.publishCommentCreatedEvent(any())).thenReturn(Mono.empty())

        commentApplicationService = CommentApplicationService(commentPersistencePort, eventPublisherPort, commentService)

        // When
        val result = commentApplicationService.createComment(postId, request, authorId, authorName)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { response ->
                response.content == "Test Comment" &&
                response.authorName == "Test User"
            }
            .verifyComplete()

        verify(commentPersistencePort).save(any())
        verify(eventPublisherPort).publishCommentCreatedEvent(any())
    }

    @Test
    fun `createComment should fail when validation fails`() {
        // Given
        val request = CreateCommentRequest(
            content = "",
            parentId = null
        )
        val postId = "post-1"
        val authorId = "user-1"
        val authorName = "Test User"

        `when`(commentService.validateCommentCreation(any(), any())).thenReturn(false)

        commentApplicationService = CommentApplicationService(commentPersistencePort, eventPublisherPort, commentService)

        // When
        val result = commentApplicationService.createComment(postId, request, authorId, authorName)

        // Then
        StepVerifier.create(result)
            .expectError(IllegalArgumentException::class.java)
            .verify()
    }

    private fun createMockComment() = com.kominioai.domain.bulletin.domain.model.Comment(
        id = CommentId("comment-1"),
        postId = PostId("post-1"),
        parentId = null,
        content = "Test Comment",
        authorId = "user-1",
        authorName = "Test User",
        likeCount = 0,
        replyCount = 0,
        createdAt = java.time.LocalDateTime.now(),
        updatedAt = java.time.LocalDateTime.now()
    )
}
