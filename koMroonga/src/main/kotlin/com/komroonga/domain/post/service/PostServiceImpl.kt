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
    private val cacheTtl = Duration.ofMinutes(30)
    private val postDispatcher = Dispatchers.IO.limitedParallelism(12)
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

    @Transactional
    suspend fun createBatch(requests: List<PostRequest>): List<Result<PostResponse>> {
        val posts = requests.map { request ->
            validateRequest(request).getOrThrow()
            val authorId = request.authorId ?: throw PostError.InvalidInput("authorId", "작성자 ID는 필수입니다")
            val author = memberCache[authorId] ?: throw PostError.NotFound(authorId)
            Post(title = request.title, content = request.content, author = author)
        }
        val savedPosts = postRepository.saveAll(posts) // 배치 삽입 활용
        val responses = savedPosts.map { Result.success(postToResponse(it)) }

        if (cacheService.enableCaching) {
            val cacheItems = savedPosts.associate { it.id!! to postToResponse(it) }
            cacheService.putBulkInCache(cacheItems, "post", cacheTtl)
        }
        return responses
    }

    private fun createPost(request: PostRequest, author: Member): Result<Post> = runCatching {
        val post = Post(title = request.title, content = request.content, author = author)
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

    fun clearMemberCache() {
        memberCache.clear()
    }

    // 멤버 캐시 사전 로드 메서드 추가
    suspend fun preloadMemberCache(memberIds: List<Long>) {
        memberIds.chunked(1000).forEach { chunk ->
            chunk.map { id ->
                memberService.findMemberEntityById(id).getOrThrow()
            }.forEach { member ->
                memberCache[member.id!!] = member
            }
        }
    }
}
