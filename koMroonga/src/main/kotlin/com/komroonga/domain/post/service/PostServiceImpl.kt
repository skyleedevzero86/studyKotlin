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
import com.komroonga.global.utils.withLogging
import com.komroonga.member.entity.Member
import com.komroonga.member.service.MemberService
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * 게시글 서비스 구현 클래스
 * 게시글 관련 비즈니스 로직 처리
 */
@Service
class PostServiceImpl(
    private val postRepository: PostRepository,
    private val cacheService: CacheService,
    private val memberService: MemberService,
    @PersistenceContext private val entityManager: EntityManager
) : PostService {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    private val cacheTtl = Duration.ofMinutes(30)
    private val postDispatcher = Dispatchers.IO.limitedParallelism(16)
    private val memberCache = Caffeine.newBuilder()
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .maximumSize(10_000)
        .build<Long, Member>()

    /**
     * 게시글 수 조회
     * @return 게시글 수
     */
    override suspend fun count(): Long =
        runCatching { postRepository.count() }
            .getOrElse { e -> logger.error("게시글 수 조회 실패: ${e.message}", e); 0L }

    /**
     * 게시글 생성
     * @param request 게시글 요청 객체
     * @return 생성된 게시글 정보
     */
    override suspend fun create(request: PostRequest): Result<PostResponse> = withContext(postDispatcher) {
        validateRequest(request)
            .fold(
                onSuccess = { req ->
                    req.authorId?.let { authorId ->
                        memberService.findMemberEntityById(authorId).fold(
                            onSuccess = { author -> createPost(req, author).map { postToResponse(it) } },
                            onFailure = { Result.failure(it) }
                        )
                    } ?: Result.failure(PostError.InvalidInput("authorId", "작성자 ID는 필수입니다"))
                },
                onFailure = { Result.failure(it) }
            )
            .withLogging(logger, "게시글 생성")
    }

    /**
     * 배치 게시글 생성
     * @param requests 게시글 요청 객체 리스트
     * @return 생성된 게시글 정보 리스트
     */
    @Transactional
    suspend fun createBatch(requests: List<PostRequest>): List<Result<PostResponse>> =
        requests.map { request ->
            validateRequest(request).map { req ->
                val authorId = req.authorId ?: throw PostError.InvalidInput("authorId", "작성자 ID는 필수입니다")
                val author = memberCache.getIfPresent(authorId) ?: memberService.findMemberEntityById(authorId)
                    .getOrThrow()
                    .also { memberCache.put(authorId, it) }
                Post(
                    title = req.title,
                    content = req.content,
                    author = author,
                    isPrivate = req.isPrivate,
                    noticeType = req.noticeType
                )
            }
        }.let { posts ->
            postRepository.saveAll(posts.map { it.getOrThrow() })
                .map { Result.success(postToResponse(it)) }
                .also { responses ->
                    if (cacheService.enableCaching) {
                        val cacheItems = responses.filter { it.isSuccess }
                            .associate { res -> res.getOrThrow().id to res.getOrThrow() }
                        cacheService.putBulkInCache(cacheItems, "post", cacheTtl)
                    }
                    entityManager.flush()
                    entityManager.clear()
                }
        }

    /**
     * 게시글 생성 로직
     * @param request 게시글 요청 객체
     * @param author 작성자 엔티티
     * @return 생성된 게시글
     */
    private fun createPost(request: PostRequest, author: Member): Result<Post> = runCatching {
        postRepository.save(
            Post(
                title = request.title,
                content = request.content,
                author = author,
                isPrivate = request.isPrivate,
                noticeType = request.noticeType
            )
        )
    }

    /**
     * 게시글 요청 유효성 검사
     * @param request 게시글 요청 객체
     * @return 유효성 검사 결과
     */
    private fun validateRequest(request: PostRequest): Result<PostRequest> = when {
        request.title.isBlank() -> Result.failure(PostError.InvalidInput("title", "제목은 비어 있을 수 없습니다"))
        request.content.isBlank() -> Result.failure(PostError.InvalidInput("content", "내용은 비어 있을 수 없습니다"))
        request.authorId == null -> Result.failure(PostError.InvalidInput("authorId", "작성자 ID는 필수입니다"))
        else -> Result.success(request)
    }

    /**
     * 게시글 엔티티를 응답 객체로 변환
     * @param post 게시글 엔티티
     * @return 게시글 응답 객체
     */
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

    /**
     * ID로 게시글 조회
     * @param id 게시글 ID
     * @param currentUser 현재 사용자
     * @return 게시글 정보
     */
    override suspend fun findById(id: Long, currentUser: MemberResponse?): Result<PostResponse> =
        withContext(postDispatcher) {
            cacheService.getCachedOrCompute("post:$id", cacheTtl) {
                fetchPostById(id, currentUser).getOrThrow()
            }
        }.withLogging(logger, "ID로 게시글 조회")

    /**
     * 게시글 조회 로직
     * @param id 게시글 ID
     * @param currentUser 현재 사용자
     * @return 게시글 정보
     */
    private suspend fun fetchPostById(id: Long, currentUser: MemberResponse?): Result<PostResponse> = runCatching {
        val post = postRepository.findById(id).orElseThrow { PostError.NotFound(id) }
        if (post.isPrivate && (currentUser == null || (post.author.id != currentUser.id && currentUser.role != com.komroonga.member.entity.Role.ROLE_ADMIN))) {
            throw PostError.Unauthorized("비공개 게시글에 접근할 수 없습니다")
        }
        if (post.noticeType == NoticeType.MEMBER && currentUser == null) {
            throw PostError.Unauthorized("회원 공지에 접근할 수 없습니다")
        }
        postToResponse(post)
    }

    /**
     * 모든 게시글 조회
     * @param currentUser 현재 사용자
     * @return 게시글 정보 스트림
     */
    override suspend fun findAll(currentUser: MemberResponse?): Flow<PostResponse> = flow {
        postRepository.findVisiblePosts(currentUser?.id)
            .map { postToResponse(it) }
            .forEach { emit(it) }
    }

    /**
     * 게시글 수정
     * @param postId 게시글 ID
     * @param memberResponse 사용자 정보
     * @param title 제목
     * @param content 내용
     * @param isPrivate 비공개 여부
     * @param noticeType 공지 유형
     * @return 수정된 게시글 정보
     */
    override suspend fun edit(
        postId: Long,
        memberResponse: MemberResponse,
        title: String,
        content: String,
        isPrivate: Boolean,
        noticeType: NoticeType
    ): Result<PostResponse> = withContext(postDispatcher) {
        validateRequest(PostRequest(title, content, memberResponse.id, isPrivate, noticeType))
            .fold(
                onSuccess = { req ->
                    runCatching {
                        val post = postRepository.findById(postId).orElseThrow { PostError.NotFound(postId) }
                        if (post.author.id != memberResponse.id && memberResponse.role != com.komroonga.member.entity.Role.ROLE_ADMIN) {
                            throw PostError.Unauthorized("게시글 수정 권한이 없습니다")
                        }
                        if (memberResponse.role != com.komroonga.member.entity.Role.ROLE_ADMIN &&
                            post.noticeType != noticeType && noticeType != NoticeType.NONE
                        ) {
                            throw PostError.Unauthorized("공지 유형 변경 권한이 없습니다")
                        }
                        postRepository.save(
                            post.copy(
                                title = req.title,
                                content = req.content,
                                isPrivate = req.isPrivate,
                                noticeType = req.noticeType
                            )
                        )
                    }.map { postToResponse(it) }
                },
                onFailure = { Result.failure(it) }
            )
            .withLogging(logger, "게시글 수정")
    }

    /**
     * 게시글 검색
     * @param searchType 검색 유형
     * @param keyword 검색어
     * @param currentUser 현재 사용자
     * @return 검색된 게시글 정보 스트림
     */
    override suspend fun search(
        searchType: String,
        keyword: String,
        currentUser: MemberResponse?
    ): Flow<PostResponse> = flow {
        postRepository.search(searchType, keyword)
            .filter { post ->
                if (post.isPrivate) currentUser?.let { it.id == post.author.id || it.role == com.komroonga.member.entity.Role.ROLE_ADMIN } ?: false
                else if (post.noticeType == NoticeType.MEMBER) currentUser != null
                else true
            }
            .map { postToResponse(it) }
            .forEach { emit(it) }
    }

    /**
     * 공지 유형별 게시글 조회
     * @param noticeType 공지 유형
     * @param currentUser 현재 사용자
     * @return 게시글 정보 스트림
     */
    override suspend fun findByNoticeType(
        noticeType: NoticeType,
        currentUser: MemberResponse?
    ): Flow<PostResponse> = flow {
        postRepository.findVisiblePostsByNoticeType(noticeType, currentUser?.id)
            .map { postToResponse(it) }
            .forEach { emit(it) }
    }

    /**
     * 회원 캐시 초기화
     */
    fun clearMemberCache() {
        memberCache.invalidateAll()
    }

    /**
     * 회원 캐시 프리로딩
     * @param memberIds 회원 ID 리스트
     */
    suspend fun preloadMemberCache(memberIds: List<Long>) {
        memberIds.chunked(1000).forEach { chunk ->
            chunk.map { id -> memberService.findMemberEntityById(id).getOrThrow() }
                .forEach { member -> memberCache.put(member.id!!, member) }
        }
    }
}