package com.komroonga.domain.post.service

import com.komroonga.domain.post.dto.PostRequest
import com.komroonga.domain.post.repository.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import com.komroonga.global.error.types.PostError
import com.komroonga.global.utils.CacheService
import com.komroonga.domain.post.entity.Post

@Service
class PostServiceImpl<PostResponse>(
    private val postRepository: PostRepository,
    private val cacheService: CacheService
) : PostService {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val cacheTtl = Duration.ofHours(1)

    override suspend fun create(request: PostRequest): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            logger.info("게시글 작성 요청: title=${request.title}")
            validateRequest(request)
            val post = Post(
                title = request.title,
                content = request.content,
                author = Post.Member(id = request.authorId, username = "testuser", password = "")
            )
            val saved = postRepository.save(post)
            logger.info("게시글 작성 성공: id=${saved.id}")
            cacheService.putInCache("post:${saved.id}", PostResponse(saved.id!!, saved.title, saved.content, saved.author.username), cacheTtl)
            PostResponse(saved.id, saved.title, saved.content, saved.author.username)
        }
    }.onFailure { logger.error("게시글 작성 실패: ${it.message}", it) }

    override suspend fun findById(id: Long): PostResult<PostResponse> = runCatching {
        cacheService.getCachedOrCompute(
            key = "post:$id",
            ttl = cacheTtl
        ) {
            withContext(Dispatchers.IO) {
                logger.info("게시글 조회 요청: id=$id")
                val post = postRepository.findById(id)
                    ?: throw PostError.NotFound(id)
                PostResponse(post.id!!, post.title, post.content, post.author.username)
            }
        }.getOrThrow()
    }.onFailure { logger.error("게시글 조회 실패: ${it.message}", it) }

    override suspend fun findAll(): Flow<PostResponse> {
        logger.info("전체 게시글 조회 요청")
        return postRepository.findAll()
            .map { PostResponse(it.id!!, it.title, it.content, it.author.username) }
            .also { logger.info("전체 게시글 조회 완료") }
    }

    private fun validateRequest(request: PostRequest) {
        if (request.title.isBlank()) throw PostError.InvalidInput("title", "제목은 비어 있을 수 없습니다")
        if (request.content.isBlank()) throw PostError.InvalidInput("content", "내용은 비어 있을 수 없습니다")
    }
}