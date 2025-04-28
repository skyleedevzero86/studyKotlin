package com.komroonga.domain.post.service

import com.komroonga.domain.member.dto.MemberResponse
import com.komroonga.domain.post.dto.PostRequest
import com.komroonga.domain.post.dto.PostResponse
import com.komroonga.domain.post.repository.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import com.komroonga.global.error.types.PostError
import com.komroonga.global.utils.CacheService
import com.komroonga.domain.post.entity.Post

@Service
class PostServiceImpl(
    private val postRepository: PostRepository,
    private val cacheService: CacheService
) : PostService {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val cacheTtl = Duration.ofHours(1)

    override suspend fun count(): Long = withContext(Dispatchers.IO) {
        postRepository.count()
    }

    override suspend fun create(request: PostRequest): Result<PostResponse> = runCatching {
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
            val postResponse = PostResponse(saved.id!!, saved.title, saved.content, saved.author.username)
            cacheService.putInCache("post:${saved.id}", postResponse, cacheTtl)
            postResponse
        }
    }.onFailure { logger.error("게시글 작성 실패: ${it.message}", it) }

    override suspend fun findById(id: Long): Result<PostResponse> = runCatching {
        cacheService.getCachedOrCompute(
            key = "post:$id",
            ttl = cacheTtl
        ) {
            withContext(Dispatchers.IO) {
                logger.info("게시글 조회 요청: id=$id")
                val post = postRepository.findById(id).orElseThrow { PostError.NotFound(id) }
                PostResponse(post.id!!, post.title, post.content, post.author.username)
            }
        }.getOrThrow()
    }.onFailure { logger.error("게시글 조회 실패: ${it.message}", it) }

    override suspend fun findAll(): Flow<PostResponse> {
        logger.info("전체 게시글 조회 요청")
        return flow {
            postRepository.findAll().forEach { post ->
                emit(PostResponse(post.id!!, post.title, post.content, post.author.username))
            }
            logger.info("전체 게시글 조회 완료")
        }
    }

    override suspend fun edit(memberResponse: MemberResponse, title: String, content: String): Result<PostResponse> = runCatching {
        withContext(Dispatchers.IO) {
            logger.info("게시글 수정 요청: title=$title (작성자: ${memberResponse.username})")

            // 요청 유효성 검증
            if (title.isBlank()) throw PostError.InvalidInput("title", "제목은 비어 있을 수 없습니다")
            if (content.isBlank()) throw PostError.InvalidInput("content", "내용은 비어 있을 수 없습니다")

            // MemberResponse를 Post.Member로 변환
            val postMember = Post.Member(
                id = memberResponse.id,
                username = memberResponse.username,
                password = "" // 보안상 비밀번호는 비워둡니다
            )

            // 게시글 수정
            val post = Post(
                title = title,
                content = content,
                author = postMember
            )

            // 저장 및 응답 생성
            val saved = postRepository.save(post)
            logger.info("게시글 수정 성공: id=${saved.id}")
            val postResponse = PostResponse(saved.id!!, saved.title, saved.content, saved.author.username)

            // 캐시에 저장
            cacheService.putInCache("post:${saved.id}", postResponse, cacheTtl)
            postResponse
        }
    }.onFailure { logger.error("게시글 수정 실패: ${it.message}", it) }

    private fun validateRequest(request: PostRequest) {
        if (request.title.isBlank()) throw PostError.InvalidInput("title", "제목은 비어 있을 수 없습니다")
        if (request.content.isBlank()) throw PostError.InvalidInput("content", "내용은 비어 있을 수 없습니다")
    }
}