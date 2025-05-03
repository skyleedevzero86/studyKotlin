package com.komroonga.domain.post.service

import com.github.benmanes.caffeine.cache.Caffeine
import com.komroonga.domain.member.dto.MemberResponse
import com.komroonga.domain.post.dto.PostRequest
import com.komroonga.domain.post.dto.PostResponse
import com.komroonga.domain.post.entity.NoticeType
import com.komroonga.domain.post.entity.Post
import com.komroonga.domain.post.repository.PostRepository
import com.komroonga.global.error.types.PostError
import com.komroonga.global.utils.CacheService
import com.komroonga.member.entity.Member
import com.komroonga.member.service.MemberService
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.util.concurrent.TimeUnit

@Service
class PostServiceImpl(
    private val postRepository: PostRepository,
    private val cacheService: CacheService,
    private val memberService: MemberService,
    @PersistenceContext private val entityManager: EntityManager
) : PostService {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val cacheTtl = Duration.ofMinutes(30)
    private val postDispatcher = Dispatchers.IO.limitedParallelism(16)
    private val memberCache = Caffeine.newBuilder()
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .maximumSize(10_000)
        .build<Long, Member>()

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
            val author = memberCache.getIfPresent(authorId) ?: try {
                val member = memberService.findMemberEntityById(authorId).getOrThrow()
                memberCache.put(authorId, member)
                member
            } catch (e: Exception) {
                logger.error("작성자 조회 실패: authorId=$authorId", e)
                throw PostError.NotFound(authorId)
            }
            Post(
                title = request.title,
                content = request.content,
                author = author,
                isPrivate = request.isPrivate,
                noticeType = request.noticeType
            )
        }
        val savedPosts = postRepository.saveAll(posts)
        val responses = savedPosts.map { Result.success(postToResponse(it)) }

        if (cacheService.enableCaching) {
            val cacheItems = savedPosts.associate { it.id!! to postToResponse(it) }
            cacheService.putBulkInCache(cacheItems, "post", cacheTtl)
        }
        entityManager.clear()
        return responses
    }

    private fun createPost(request: PostRequest, author: Member): Result<Post> = runCatching {
        val post = Post(
            title = request.title,
            content = request.content,
            author = author,
            isPrivate = request.isPrivate,
            noticeType = request.noticeType
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
        PostResponse(
            id = post.id!!,
            title = post.title,
            content = post.content,
            authorUsername = post.author.username,
            authorId = post.author.id!!,
            isPrivate = post.isPrivate,
            noticeType = post.noticeType
        )

    override suspend fun findById(id: Long, currentUser: MemberResponse?): Result<PostResponse> =
        cacheService.getCachedOrCompute("post:$id", cacheTtl) {
            fetchPostById(id, currentUser)
        }.getOrElse {
            logger.error("게시글 조회 실패: {}", it.message, it)
            Result.failure(PostError.NotFound(id))
        }

    private suspend fun fetchPostById(id: Long, currentUser: MemberResponse?): Result<PostResponse> = runCatching {
        val post = postRepository.findById(id).orElseThrow { PostError.NotFound(id) }

        // 비공개 게시글 접근 권한 확인
        if (post.isPrivate) {
            val userId = currentUser?.id
            // 비로그인 사용자는 비공개 게시글 접근 불가
            if (userId == null) {
                throw PostError.Unauthorized("비공개 게시글에 접근할 수 없습니다")
            }

            // 작성자나 관리자만 비공개 게시글 접근 가능
            if (post.author.id != userId && currentUser.role != com.komroonga.member.entity.Role.ROLE_ADMIN) {
                throw PostError.Unauthorized("비공개 게시글에 접근할 수 없습니다")
            }
        }

        // 회원 공지는 로그인 사용자만 접근 가능
        if (post.noticeType == NoticeType.MEMBER && currentUser == null) {
            throw PostError.Unauthorized("회원 공지에 접근할 수 없습니다")
        }

        postToResponse(post)
    }

    override suspend fun findAll(currentUser: MemberResponse?): Flow<PostResponse> = flow {
        val userId = currentUser?.id
        postRepository.findVisiblePosts(userId)
            .asSequence()
            .map { post -> postToResponse(post) }
            .forEach { emit(it) }
        logger.info("전체 게시글 조회 완료")
    }

    override suspend fun edit(
        postId: Long,
        memberResponse: MemberResponse,
        title: String,
        content: String,
        isPrivate: Boolean,
        noticeType: NoticeType
    ): Result<PostResponse> =
        validateRequest(PostRequest(title, content, memberResponse.id, isPrivate, noticeType))
            .map { request ->
                runCatching {
                    val post = postRepository.findById(postId)
                        .orElseThrow { PostError.NotFound(postId) }

                    // 작성자나 관리자만 게시글 수정 가능
                    if (post.author.id != memberResponse.id && memberResponse.role != com.komroonga.member.entity.Role.ROLE_ADMIN) {
                        throw PostError.Unauthorized("게시글 수정 권한이 없습니다")
                    }

                    // 관리자가 아닌 경우 공지 유형 변경 불가
                    if (memberResponse.role != com.komroonga.member.entity.Role.ROLE_ADMIN &&
                        post.noticeType != noticeType &&
                        noticeType != NoticeType.NONE) {
                        throw PostError.Unauthorized("공지 유형 변경 권한이 없습니다")
                    }

                    val updatedPost = post.copy(
                        title = request.title,
                        content = request.content,
                        isPrivate = request.isPrivate,
                        noticeType = request.noticeType
                    )
                    postRepository.save(updatedPost)
                }
            }
            .flatten()
            .map { post -> postToResponse(post) }
            .onFailure { e ->
                logger.error("게시글 수정 실패: {}", e.message, e)
            }

    override suspend fun search(
        searchType: String,
        keyword: String,
        currentUser: MemberResponse?
    ): Flow<PostResponse> = flow {
        val userId = currentUser?.id

        // 검색 결과 중 접근 가능한 게시글만 필터링
        postRepository.search(searchType, keyword)
            .filter { post ->
                // 비공개 게시글은 작성자나 관리자만 볼 수 있음
                if (post.isPrivate) {
                    userId != null && (post.author.id == userId || currentUser.role == com.komroonga.member.entity.Role.ROLE_ADMIN)
                }
                // 회원 공지는 로그인 사용자만 볼 수 있음
                else if (post.noticeType == NoticeType.MEMBER) {
                    userId != null
                }
                // 그 외에는 모두 볼 수 있음
                else {
                    true
                }
            }
            .map { postToResponse(it) }
            .forEach { emit(it) }

        logger.info("게시글 검색 완료: 검색 유형=$searchType, 키워드=$keyword")
    }

    override suspend fun findByNoticeType(
        noticeType: NoticeType,
        currentUser: MemberResponse?
    ): Flow<PostResponse> = flow {
        val userId = currentUser?.id

        postRepository.findVisiblePostsByNoticeType(noticeType, userId)
            .map { postToResponse(it) }
            .forEach { emit(it) }

        logger.info("공지 유형별 게시글 조회 완료: 공지 유형=$noticeType")
    }

    private inline fun <T> Result<Result<T>>.flatten(): Result<T> =
        fold(
            onSuccess = { it },
            onFailure = { Result.failure(it) }
        )

    fun clearMemberCache() {
        memberCache.invalidateAll()
    }

    suspend fun preloadMemberCache(memberIds: List<Long>) {
        memberIds.chunked(1000).forEach { chunk ->
            chunk.map { id ->
                memberService.findMemberEntityById(id).getOrThrow()
            }.forEach { member ->
                memberCache.put(member.id!!, member)
            }
        }
    }
}