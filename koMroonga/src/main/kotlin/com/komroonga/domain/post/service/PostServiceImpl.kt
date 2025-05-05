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
import org.springframework.transaction.support.TransactionTemplate
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
    private val transactionTemplate: TransactionTemplate, // TransactionTemplate 주입 추가
    @PersistenceContext private val entityManager: EntityManager
) : PostService {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    private val cacheTtl = Duration.ofMinutes(30)
    private val postDispatcher = Dispatchers.IO.limitedParallelism(16)
    private val memberCache = Caffeine.newBuilder()
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .maximumSize(10_000)
        .build<Long, Member>()
    private var cachingEnabled = true // 캐싱 상태 관리

    companion object {
        private const val BATCH_SIZE = 2_000 // 배치 크기 정의
    }

    /**
     * 배치 데이터 구조를 위한 데이터 클래스
     */
    data class PostBatchData(
        val title: String,
        val content: String,
        val authorId: Long,
        val isPrivate: Boolean,
        val noticeType: String
    )

    /**
     * 캐싱 상태 조회
     */
    fun getCachingState(): Boolean = cachingEnabled

    /**
     * 캐싱 상태 설정
     */
    fun setCachingState(enabled: Boolean) {
        cachingEnabled = enabled
        cacheService.enableCaching = enabled
    }

    /**
     * 초기화 완료 후 캐시 채우기
     */
    suspend fun populateCacheAfterInitialization() {
        logger.info("게시글 캐시 채우기 시작")
        val posts = postRepository.findAll()
        val cacheItems = posts.associate { it.id!! to postToResponse(it) }
        cacheService.putBulkInCache(cacheItems, "post", cacheTtl)
        logger.info("게시글 캐시 채우기 완료: ${cacheItems.size}개")
    }

    /**
     * 게시글 수 조회
     */
    override suspend fun count(): Long =
        runCatching { postRepository.count() }
            .getOrElse { e -> logger.error("게시글 수 조회 실패: ${e.message}", e); 0L }

    /**
     * 게시글 생성
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
     */

    @Transactional
    suspend fun createBatch(requests: List<PostRequest>): List<Result<PostResponse>> {
        val responses = mutableListOf<Result<PostResponse>>()

        requests.map { request ->
            validateRequest(request).map { req ->
                val authorId = req.authorId ?: throw PostError.InvalidInput("authorId", "작성자 ID는 필수입니다")
                val author = memberCache.getIfPresent(authorId) ?: memberService.findMemberEntityById(authorId)
                    .getOrThrow()
                    .also { memberCache.put(authorId, it) }
                val noticeTypeName = req.noticeType.name
                logger.debug("Creating PostBatchData with noticeType: $noticeTypeName")
                PostBatchData(
                    title = req.title,
                    content = req.content,
                    authorId = authorId,
                    isPrivate = req.isPrivate,
                    noticeType = noticeTypeName
                )
            }
        }.chunked(BATCH_SIZE).forEach { batch ->
            try {
                transactionTemplate.execute {
                    batch.forEach { result ->
                        result.fold(
                            onSuccess = { data ->
                                logger.info("Processing PostBatchData: title=${data.title}, noticeType=${data.noticeType}")
                                try {
                                    // noticeType 유효성 검사
                                    if (data.noticeType.isBlank()) {
                                        logger.error("noticeType이 비어 있습니다")
                                        throw PostError.InvalidInput("noticeType", "공지 유형은 비어 있을 수 없습니다")
                                    }
                                    val validNoticeTypes = NoticeType.entries.map { it.name }
                                    if (data.noticeType !in validNoticeTypes) {
                                        logger.error("유효하지 않은 noticeType: ${data.noticeType}")
                                        throw PostError.InvalidInput("noticeType", "유효하지 않은 공지 유형: ${data.noticeType}. 허용 값: $validNoticeTypes")
                                    }

                                    // NoticeType.valueOf 호출
                                    val noticeTypeEnum = try {
                                        NoticeType.valueOf(data.noticeType)
                                    } catch (e: IllegalArgumentException) {
                                        logger.error("NoticeType.valueOf 실패: ${data.noticeType}")
                                        throw PostError.InvalidInput("noticeType", "유효하지 않은 공지 유형: ${data.noticeType}. 허용 값: $validNoticeTypes")
                                    }

                                    logger.debug("Calling bulkInsert with noticeType: ${data.noticeType}")
                                    val rowsAffected = postRepository.bulkInsert(
                                        title = data.title,
                                        content = data.content,
                                        authorId = data.authorId,
                                        isPrivate = data.isPrivate,
                                        noticeType = data.noticeType
                                    )
                                    if (rowsAffected > 0) {
                                        logger.info("Successfully inserted post with noticeType: ${data.noticeType}")
                                        responses.add(Result.success(PostResponse(0, data.title, data.content, "user${data.authorId}", data.authorId, data.isPrivate, noticeTypeEnum)))
                                    } else {
                                        logger.error("Failed to insert post with noticeType: ${data.noticeType}")
                                        responses.add(Result.failure(Exception("삽입 실패")))
                                    }
                                } catch (e: Exception) {
                                    logger.error("게시글 삽입 실패: ${e.message}", e)
                                    responses.add(Result.failure(e))
                                }
                            },
                            onFailure = { responses.add(Result.failure(it)) }
                        )
                    }
                    entityManager.flush()
                    entityManager.clear()
                    true // 트랜잭션 커밋
                }
            } catch (e: Exception) {
                logger.error("배치 처리 중 오류 발생: ${e.message}", e)
            }
        }

        return responses
    }

    /**
     * 게시글 생성 로직
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
     */
    private fun validateRequest(request: PostRequest): Result<PostRequest> = when {
        request.title.isBlank() -> Result.failure(PostError.InvalidInput("title", "제목은 비어 있을 수 없습니다"))
        request.content.isBlank() -> Result.failure(PostError.InvalidInput("content", "내용은 비어 있을 수 없습니다"))
        request.authorId == null -> Result.failure(PostError.InvalidInput("authorId", "작성자 ID는 필수입니다"))
        else -> Result.success(request)
    }

    /**
     * 게시글 엔티티를 응답 객체로 변환
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
     */
    override suspend fun findById(id: Long, currentUser: MemberResponse?): Result<PostResponse> =
        withContext(postDispatcher) {
            cacheService.getCachedOrCompute("post:$id", cacheTtl) {
                fetchPostById(id, currentUser).getOrThrow()
            }
        }.withLogging(logger, "ID로 게시글 조회")

    /**
     * 게시글 조회 로직
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
     */
    override suspend fun findAll(currentUser: MemberResponse?): Flow<PostResponse> = flow {
        postRepository.findVisiblePosts(currentUser?.id)
            .map { postToResponse(it) }
            .forEach { emit(it) }
    }

    /**
     * 게시글 수정
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
     */
    suspend fun preloadMemberCache(memberIds: List<Long>) {
        // 단일 쿼리로 모든 회원 로드
        val members = memberService.findAllByIds(memberIds)
        members.forEach { member -> memberCache.put(member.id!!, member) }
        logger.info("회원 캐시 프리로딩 완료: ${members.size}명")
    }
}