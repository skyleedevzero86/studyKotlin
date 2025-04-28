package com.komroonga.domain.post.service

import com.komroonga.domain.member.dto.MemberResponse
import com.komroonga.domain.post.dto.PostRequest
import com.komroonga.domain.post.dto.PostResponse
import com.komroonga.domain.post.entity.Post
import com.komroonga.domain.post.repository.PostRepository
import com.komroonga.global.error.types.PostError
import com.komroonga.global.utils.CacheService
import com.komroonga.member.entity.Member
import com.komroonga.member.service.MemberService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Service
class PostServiceImpl(
    private val postRepository: PostRepository,
    private val cacheService: CacheService,
    private val memberService: MemberService,
) : PostService {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val cacheTtl = Duration.ofMinutes(30) // 캐시 시간 단축
    private val postDispatcher = Dispatchers.IO.limitedParallelism(12)

    // 멤버 ID 캐시 (초기화 과정에서 사용)
    private val memberCache = ConcurrentHashMap<Long, Member>()

    override suspend fun count(): Long = runCatching {
        postRepository.count()
    }.getOrElse {
        logger.error("게시글 수 조회 실패: {}", it.message, it)
        0L
    }

    override suspend fun create(request: PostRequest): Result<PostResponse> =
        validateRequest(request)
            .mapCatching { req ->
                val author = memberService.findMemberEntityById(req.authorId ?: throw PostError.InvalidInput("authorId", "작성자 ID는 필수입니다"))
                    .getOrThrow()
                createPost(req, author).getOrThrow()
            }
            .map { post -> postToResponse(post) }
            .onFailure { e ->
                logger.error("게시글 작성 실패: {}", e.message, e)
            }

    // 벌크 저장 기능 추가
    @Transactional
    suspend fun createBatch(requests: List<PostRequest>): List<Result<PostResponse>> {
        val results = mutableListOf<Result<PostResponse>>()
        val cacheItems = mutableMapOf<Long, PostResponse>()

        for ((index, request) in requests.withIndex()) {
            validateRequest(request).fold(
                onSuccess = { validated ->
                    try {
                        // 코루틴 내부에서 suspend 함수 호출
                        val authorId = validated.authorId ?: throw PostError.InvalidInput("authorId", "작성자 ID는 필수입니다")

                        // memberCache에서 먼저 찾고, 없으면 withContext를 사용하여 suspend 함수 호출
                        val author = memberCache[authorId] ?: withContext(Dispatchers.Default) {
                            val member = memberService.findMemberEntityById(authorId).getOrThrow()
                            memberCache[authorId] = member
                            member
                        }

                        val post = Post(
                            title = validated.title,
                            content = validated.content,
                            author = author
                        )
                        val savedPost = postRepository.save(post)
                        val response = postToResponse(savedPost)

                        // 캐시 데이터 준비
                        cacheItems[savedPost.id!!] = response
                        results.add(Result.success(response))

                        if ((index + 1) % 1000 == 0) {
                            logger.info("Created batch posts: {}/{}", index + 1, requests.size)
                            true
                        } else {
                            false
                        }
                    } catch (e: Exception) {
                        logger.error("Batch 게시글 생성 실패 (index: $index): ${e.message}", e)
                        results.add(Result.failure(e))
                    }
                },
                onFailure = { error ->
                    logger.error("Batch 게시글 검증 실패 (index: $index): ${error.message}", error)
                    results.add(Result.failure(error))
                }
            )
        }

        // 벌크 캐싱
        if (cacheItems.isNotEmpty()) {
            if (cacheService.enableCaching) {
                cacheService.putBulkInCache(cacheItems, "post", cacheTtl)
            } else {
                logger.debug("Caching is disabled, skipping bulk cache update")
            }
        }

        return results
    }

    private fun createPost(request: PostRequest, author: Member): Result<Post> = runCatching {
        val post = Post(
            title = request.title,
            content = request.content,
            author = author
        )
        postRepository.save(post)
    }

    private fun validateRequest(request: PostRequest): Result<PostRequest> = when {
        request.title.isBlank() -> Result.failure(PostError.InvalidInput("title", "제목은 비어 있을 수 없습니다"))
        request.content.isBlank() -> Result.failure(PostError.InvalidInput("content", "내용은 비어 있을 수 없습니다"))
        request.authorId == null -> Result.failure(PostError.InvalidInput("authorId", "작성자 ID는 필수입니다"))
        else -> Result.success(request)
    }

    private fun postToResponse(post: Post): PostResponse =
        PostResponse(post.id!!, post.title, post.content, post.author.username)

    override suspend fun findById(id: Long): Result<PostResponse> =
        cacheService.getCachedOrCompute("post:$id", cacheTtl) {
            fetchPostById(id)
        }.getOrElse {
            logger.error("게시글 조회 실패: {}", it.message, it)
            Result.failure(PostError.NotFound(id))
        }

    private suspend fun fetchPostById(id: Long): Result<PostResponse> = runCatching {
        val post = postRepository.findById(id).orElseThrow { PostError.NotFound(id) }
        postToResponse(post)
    }

    override suspend fun findAll(): Flow<PostResponse> = flow {
        postRepository.findAll()
            .asSequence()
            .map { post -> postToResponse(post) }
            .forEach { emit(it) }
        logger.info("전체 게시글 조회 완료")
    }

    override suspend fun edit(postId: Long, memberResponse: MemberResponse, title: String, content: String): Result<PostResponse> =
        validateRequest(PostRequest(title, content, memberResponse.id))
            .map { request ->
                runCatching {
                    val post = postRepository.findById(postId)
                        .orElseThrow { PostError.NotFound(postId) }
                    if (post.author.id != memberResponse.id) {
                        throw PostError.Unauthorized("게시글 수정 권한이 없습니다")
                    }
                    post.copy(title = request.title, content = request.content)
                    postRepository.save(post)
                }
            }
            .flatten()
            .map { post -> postToResponse(post) }
            .onFailure { e ->
                logger.error("게시글 수정 실패: {}", e.message, e)
            }

    private inline fun <T> Result<Result<T>>.flatten(): Result<T> =
        fold(
            onSuccess = { it },
            onFailure = { Result.failure(it) }
        )

    // 초기화용 유틸리티 메서드
    fun clearMemberCache() {
        memberCache.clear()
    }
}