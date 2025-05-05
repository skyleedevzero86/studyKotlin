package com.komroonga.global.inits

import com.komroonga.domain.member.dto.MemberRequest
import com.komroonga.domain.post.dto.PostRequest
import com.komroonga.domain.post.entity.NoticeType
import com.komroonga.domain.post.service.PostService
import com.komroonga.domain.post.service.PostServiceImpl
import com.komroonga.global.utils.PerformanceMetrics
import com.komroonga.member.entity.Role
import com.komroonga.member.service.MemberService
import com.komroonga.member.service.MemberServiceImpl
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID
import kotlin.random.Random
import kotlin.system.measureTimeMillis

/**
 * 데이터 초기화를 위한 설정 클래스
 * JPA를 사용하여  4500 명의? 사용자와 30만? 개의 게시글을 효율적으로 삽입
 */
@Configuration
class InitData(
    private val memberService: MemberService,
    private val postService: PostService,
    @PersistenceContext private val entityManager: EntityManager,
    private val transactionTemplate: TransactionTemplate
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        private const val MEMBER_COUNT = 4_500 // 사용자 수
        private const val POSTS_PER_MEMBER = 67 // 사용자당 게시글 수
        private const val BATCH_SIZE = 2_000 // 배치 크기
    }

    private val sampleTitles = listOf(
        "봄날의 산책", "고양이 일기", "커피 한 잔의 여유", "서울 여행기",
        "스터디 후기", "운동 루틴 공유", "책 리뷰", "비 오는 날",
        "맛집 탐방", "개발 시작"
    )

    private val sampleContents = listOf(
        "날씨가 좋아서 공원에서 산책을 했어요.",
        "우리집 고양이가 오늘도 귀엽게 울었어요.",
        "아침에 마시는 따뜻한 라떼 한 잔은 정말 행복하네요.",
        "경복궁과 북촌 한옥마을을 다녀왔습니다.",
        "오늘은 알고리즘 문제를 풀면서 팀원들과 많이 소통했어요.",
        "요즘 홈트에 빠졌어요. 하루 30분씩 스트레칭과 유산소!",
        "작은 습관이 인생을 바꾸는 데 얼마나 중요한지 알게 됐어요.",
        "비가 와서 창밖을 보며 음악을 들었어요. 감성 가득한 하루.",
        "맛있는 음식을 먹었어요. 또 가고 싶어요!",
        "새로운 기술을 배우기 시작했어요. 정말 흥미롭네요."
    )

    /**
     * 사용자 요청 생성
     */
    private fun generateMemberRequest(index: Int): MemberRequest = MemberRequest(
        username = "user$index",
        password = "pass%02d".format(index % 100),
        email = "user$index@example.com",
        name = "사용자 $index",
        role = Role.ROLE_USER
    )

    /**
     * 사용자 배치 생성
     */
    private fun generateMemberBatches(): List<List<MemberRequest>> =
        (1..MEMBER_COUNT).chunked(BATCH_SIZE).map { range ->
            range.map { generateMemberRequest(it) }
        }

    /**
     * 게시글 요청 생성
     */
    private fun generatePostRequest(memberId: Long): PostRequest {
        val uniqueId = UUID.randomUUID().toString().substring(0, 8)
        return PostRequest(
            title = "${sampleTitles.random()} #$uniqueId",
            content = "${sampleContents.random()} (작성자: user$memberId)",
            authorId = memberId,
            isPrivate = Random.nextInt(10) < 2,
            noticeType = NoticeType.NONE
        )
    }

    /**
     * 게시글 배치 생성
     */
    private fun generatePostBatches(memberCount: Int, adminIds: List<Long>): List<List<PostRequest>> {
        val batches = mutableListOf<List<PostRequest>>()

        // 관리자 공지사항
        val adminPosts = adminIds.flatMap { adminId ->
            listOf(
                PostRequest(
                    title = "전체 공지사항 - 관리자 $adminId",
                    content = "중요한 전체 공지사항입니다.",
                    authorId = adminId,
                    isPrivate = false,
                    noticeType = NoticeType.ALL
                ),
                PostRequest(
                    title = "회원 공지사항 - 관리자 $adminId",
                    content = "회원 전용 공지사항입니다.",
                    authorId = adminId,
                    isPrivate = false,
                    noticeType = NoticeType.MEMBER
                )
            )
        }
        if (adminPosts.isNotEmpty()) batches.add(adminPosts)

        // 일반 사용자 게시글
        val totalPosts = memberCount * POSTS_PER_MEMBER
        (0 until totalPosts).chunked(BATCH_SIZE).forEach { range ->
            val batch = range.map { i ->
                val memberId = (i % memberCount) + 1
                generatePostRequest(memberId.toLong())
            }
            batches.add(batch)
        }
        return batches
    }

    /**
     * 사용자 초기화
     */
    @Transactional
    fun initializeMembers(): Triple<Long, Result<Unit>, List<Long>> {
        logger.info("사용자 생성 시작: 총 $MEMBER_COUNT 명, 배치 크기: $BATCH_SIZE")
        val adminIds = mutableListOf<Long>()

        val time = measureTimeMillis {
            // 관리자 계정 삽입
            val admins = listOf(
                MemberRequest(username = "admin1", password = "adminpass1", role = Role.ROLE_ADMIN, email = "admin1@example.com", name = "관리자 1"),
                MemberRequest(username = "admin2", password = "adminpass2", role = Role.ROLE_ADMIN, email = "admin2@example.com", name = "관리자 2")
            )
            transactionTemplate.execute {
                runBlocking {
                    (memberService as? MemberServiceImpl)?.registerBatch(admins)?.map { it.id }?.let { adminIds.addAll(it) }
                        ?: throw IllegalStateException("MemberService는 MemberServiceImpl이어야 함")
                }
            }

            // 일반 사용자 삽입
            val memberBatches = generateMemberBatches()
            memberBatches.forEachIndexed { index, batch ->
                transactionTemplate.execute {
                    runBlocking {
                        try {
                            (memberService as MemberServiceImpl).registerBatch(batch)
                            entityManager.flush()
                            entityManager.clear()
                        } catch (e: Exception) {
                            logger.error("사용자 배치 처리 실패: ${e.message}", e)
                            throw e
                        }
                    }
                }
                System.gc()
                val progress = ((index + 1) * 100) / memberBatches.size
                logger.info("사용자 생성 진행률: $progress%")
            }
        }

        logger.info("사용자 생성 완료: 소요 시간 ${time / 1000}초")
        return Triple(time, Result.success(Unit), adminIds)
    }

    /**
     * 게시글 초기화
     */
    @Transactional
    suspend fun initializePosts(adminIds: List<Long>): Pair<Long, Result<Unit>> {
        val currentCount = postService.count()
        if (currentCount > 0) {
            logger.info("게시글 존재: $currentCount 개, 초기화 건너뜀")
            return 0L to Result.success(Unit)
        }

        val memberCount = memberService.count().getOrDefault(0L)
        if (memberCount <= 0) {
            logger.warn("사용자 없음, 게시글 초기화 건너뜀")
            return 0L to Result.failure(IllegalStateException("사용자 없음"))
        }

        logger.info("게시글 생성 시작: 총 ${memberCount * POSTS_PER_MEMBER} 개")
        val postServiceImpl = postService as? PostServiceImpl ?: throw IllegalStateException("PostService는 PostServiceImpl이어야 함")

        val regularUserCount = memberCount - adminIds.size
        postServiceImpl.preloadMemberCache((1..regularUserCount).map { it.toLong() } + adminIds)

        val time = measureTimeMillis {
            val postBatches = generatePostBatches(regularUserCount.toInt(), adminIds)
            postBatches.forEachIndexed { index, batch ->
                transactionTemplate.execute {
                    runBlocking {
                        try {
                            postServiceImpl.createBatch(batch)
                            entityManager.flush()
                            entityManager.clear()
                        } catch (e: Exception) {
                            logger.error("게시글 배치 처리 실패: ${e.message}", e)
                            throw e
                        }
                    }
                }
                System.gc()
                val progress = ((index + 1) * 100) / postBatches.size
                logger.info("게시글 생성 진행률: $progress%")
            }
        }

        postServiceImpl.clearMemberCache()
        logger.info("게시글 생성 완료: 소요 시간 ${time / 1000}초")
        return time to Result.success(Unit)
    }

    /**
     * 애플리케이션 실행 시 데이터 초기화
     */
    @Bean
    fun initDataApplicationRunner(): ApplicationRunner = ApplicationRunner {
        runBlocking {
            System.gc()
            val beforeMemory = getUsedMemoryMB()
            logger.info("데이터 초기화 시작")

            var memberTimeActual = 0L
            var postTimeActual = 0L

            val totalTime = measureTimeMillis {
                initializeMembers()
                    .also { (time, result, adminIds) ->
                        memberTimeActual = time
                        result.onSuccess {
                            logger.info("사용자 초기화 성공")
                            initializePosts(adminIds).also { (time, postResult) ->
                                postTimeActual = time
                                postResult.onFailure { e -> logger.error("게시글 초기화 오류: ${e.message}", e) }
                            }
                        }.onFailure { e -> logger.error("사용자 초기화 오류: ${e.message}", e) }
                    }
                (postService as? PostServiceImpl)?.clearMemberCache()
            }

            System.gc()
            val afterMemory = getUsedMemoryMB()

            PerformanceMetrics(
                totalTimeMs = totalTime,
                memberTimeMs = memberTimeActual,
                postTimeMs = postTimeActual,
                beforeMemoryMB = beforeMemory,
                afterMemoryMB = afterMemory
            ).generateReport().let { logger.info(it) }
        }
    }

    private fun getUsedMemoryMB(): Double =
        (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()).toDouble() / (1024 * 1024)
}