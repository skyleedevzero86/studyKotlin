package com.komroonga.global.inits

import com.komroonga.domain.member.dto.MemberRequest
import com.komroonga.domain.post.dto.PostRequest
import com.komroonga.domain.post.entity.NoticeType
import com.komroonga.domain.post.service.PostService
import com.komroonga.domain.post.service.PostServiceImpl
import com.komroonga.global.utils.CacheService
import com.komroonga.global.utils.InitializationResult
import com.komroonga.global.utils.PerformanceMetrics
import com.komroonga.member.entity.Role
import com.komroonga.member.service.MemberService
import com.komroonga.member.service.MemberServiceImpl
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import java.util.concurrent.Executors
import kotlin.math.min
import kotlin.system.measureTimeMillis
import java.time.Duration

/**
 * 데이터 초기화를 위한 설정 클래스
 * 사용자와 게시글 데이터를 생성하여 DB에 저장
 */
@Configuration
class InitData(
    private val memberService: MemberService,
    private val postService: PostService,
    private val cacheService: CacheService,
    @PersistenceContext private val entityManager: EntityManager
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        private const val MEMBER_COUNT = 100_000 // 사용자 수
        private const val POSTS_PER_MEMBER = 1 // 사용자당 게시글 수 (총 1,000,000개 게시글)
        private const val BATCH_SIZE = 5000 // 배치 크기 증가
        private const val PARALLEL_BATCHES = 4 // 병렬 처리 감소

        // 코루틴 디스패처 설정 (IO 작업 최적화)
        private val optimizedDispatcher = Dispatchers.IO.limitedParallelism(4)

        // 대량 데이터 처리용 스레드 풀
        private val bulkProcessingDispatcher = Executors.newFixedThreadPool(4)
            .asCoroutineDispatcher()
    }

    // 샘플 제목 목록
    private val sampleTitles = listOf(
        "봄날의 산책", "고양이 일기", "커피 한 잔의 여유", "서울 여행기",
        "스터디 후기", "운동 루틴 공유", "책 리뷰", "비 오는 날",
        "맛집 탐방", "개발 시작"
    )

    // 샘플 내용 목록
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
     * 사용자 배치 생성
     * @return 사용자 요청 객체 리스트의 리스트 (배치 단위)
     */
    private fun generateMemberBatches(): List<List<MemberRequest>> {
        val batches = mutableListOf<List<MemberRequest>>()
        val batchCount = (MEMBER_COUNT + BATCH_SIZE - 1) / BATCH_SIZE

        for (batchIndex in 0 until batchCount) {
            val start = batchIndex * BATCH_SIZE + 1
            val end = min(start + BATCH_SIZE - 1, MEMBER_COUNT)

            val batch = (start..end).map { i ->
                MemberRequest(
                    username = "user$i",
                    password = "pass%02d".format(i % 100),
                    email = "user$i@example.com",
                    name = "사용자 $i",
                    role = Role.ROLE_USER
                )
            }
            batches.add(batch)
        }
        return batches
    }

    /**
     * 게시글 배치 생성
     * @param memberCount 사용자 수
     * @param adminIds 관리자 ID 목록
     * @return 게시글 요청 객체 리스트의 리스트 (배치 단위)
     */
    private fun generatePostBatches(memberCount: Int, adminIds: List<Long>): List<List<PostRequest>> {
        val batches = mutableListOf<List<PostRequest>>()
        val random = java.util.Random()

        // 관리자 공지사항 게시글 생성
        val adminPosts = mutableListOf<PostRequest>()
        adminIds.forEach { adminId ->
            adminPosts.add(
                PostRequest(
                    title = "전체 공지사항 - 관리자 ${adminId}",
                    content = "중요한 전체 공지사항입니다. 모든 사용자가 확인해주세요.",
                    authorId = adminId,
                    isPrivate = false,
                    noticeType = NoticeType.ALL
                )
            )
            adminPosts.add(
                PostRequest(
                    title = "회원 공지사항 - 관리자 ${adminId}",
                    content = "회원들만 볼 수 있는 공지사항입니다.",
                    authorId = adminId,
                    isPrivate = false,
                    noticeType = NoticeType.MEMBER
                )
            )
        }
        if (adminPosts.isNotEmpty()) {
            batches.add(adminPosts)
        }

        // 일반 사용자 게시글 생성
        val totalPosts = memberCount * POSTS_PER_MEMBER
        val batchCount = (totalPosts + BATCH_SIZE - 1) / BATCH_SIZE

        for (batchIndex in 0 until batchCount) {
            val batchSize = min(BATCH_SIZE, totalPosts - batchIndex * BATCH_SIZE)
            val batch = mutableListOf<PostRequest>()

            for (i in 0 until batchSize) {
                val memberId = (batchIndex * BATCH_SIZE + i) % memberCount + 1

                val uniqueId = UUID.randomUUID().toString().substring(0, 8)
                val title = "${sampleTitles.random()} #$uniqueId"
                val content = "${sampleContents.random()} (작성자: user$memberId)"

                batch.add(
                    PostRequest(
                        title = title,
                        content = content,
                        authorId = memberId.toLong(),
                        isPrivate = random.nextInt(10) < 2, // 20% 확률로 비공개
                        noticeType = NoticeType.NONE
                    )
                )
            }
            batches.add(batch)
        }
        return batches
    }

    /**
     * 사용자 초기화
     * @return 초기화 시간, 결과, 관리자 ID 목록
     */
    @Transactional
    suspend fun initializeMembers(): Triple<Long, InitializationResult, List<Long>> = withContext(bulkProcessingDispatcher) {
        val currentCount = memberService.count()
        if (currentCount.isSuccess && (currentCount.getOrNull() ?: 0) > 0) {
            logger.info("이미 사용자가 존재합니다. 사용자 초기화를 건너뜁니다. 현재 사용자 수: ${currentCount.getOrNull()}")
            return@withContext Triple(0L, Result.success(Unit), listOf(1L, 2L))
        }

        logger.info("사용자 생성 시작... 총 {} 사용자, 배치 크기: {}, 병렬 처리: {}", MEMBER_COUNT, BATCH_SIZE, PARALLEL_BATCHES)

        val prevCachingState = cacheService.enableCaching
        cacheService.enableCaching = true

        val adminIds = mutableListOf<Long>()

        val time = measureTimeMillis {
            val admins = listOf(
                MemberRequest(
                    username = "admin1",
                    password = "adminpass1",
                    role = Role.ROLE_ADMIN,
                    email = "admin1@example.com",
                    name = "관리자 1"
                ),
                MemberRequest(
                    username = "admin2",
                    password = "adminpass2",
                    role = Role.ROLE_ADMIN,
                    email = "admin2@example.com",
                    name = "관리자 2"
                )
            )

            val memberServiceImpl = memberService as? MemberServiceImpl
                ?: throw IllegalStateException("MemberService는 MemberServiceImpl의 인스턴스여야 합니다")

            val adminResponses = memberServiceImpl.registerBatch(admins)
            adminIds.addAll(adminResponses.map { it.id })
            logger.info("관리자 계정 생성 완료: ${adminIds.joinToString()}")

            val memberBatches = generateMemberBatches()
            logger.info("사용자 배치 생성 완료. 총 {} 배치", memberBatches.size)

            memberBatches.chunked(PARALLEL_BATCHES).forEachIndexed { chunkIndex, batchChunk ->
                runBlocking {
                    val jobs = batchChunk.map { batch ->
                        launch(optimizedDispatcher) {
                            try {
                                memberServiceImpl.registerBatch(batch)
                                logger.debug("배치 처리 완료: ${batch.size}개 사용자")
                            } catch (e: Exception) {
                                logger.error("배치 처리 중 오류: ${e.message}", e)
                            }
                        }
                    }
                    jobs.joinAll()
                }

                // 메모리 관리 강화
                entityManager.clear()
                System.gc()

                val progress = min(100, ((chunkIndex + 1) * PARALLEL_BATCHES * 100) / memberBatches.size)
                logger.info("사용자 생성 진행률: {}%, 처리된 배치: {}/{}", progress, (chunkIndex + 1) * PARALLEL_BATCHES, memberBatches.size)
            }
        }

        cacheService.enableCaching = prevCachingState
        logger.info("사용자 생성 완료. 소요 시간: {}초", time / 1000)

        val finalCount = memberService.count()
        logger.info("최종 등록된 사용자 수: {}", finalCount.getOrNull())

        return@withContext Triple(time, Result.success(Unit), adminIds)
    }

    /**
     * 게시글 초기화
     * @param adminIds 관리자 ID 목록
     * @return 초기화 시간, 결과
     */
    @Transactional
    suspend fun initializePosts(adminIds: List<Long>): Pair<Long, InitializationResult> = withContext(bulkProcessingDispatcher) {
        val currentCount = postService.count()
        if (currentCount > 0) {
            logger.info("이미 게시글이 존재합니다. 게시글 초기화를 건너뜁니다. 현재 게시글 수: $currentCount")
            return@withContext 0L to Result.success(Unit)
        }

        val memberCount = memberService.count().getOrNull() ?: 0
        if (memberCount <= 0) {
            logger.warn("사용자가 존재하지 않습니다. 게시글 초기화를 건너뜁니다.")
            return@withContext 0L to Result.failure(IllegalStateException("사용자가 존재하지 않습니다"))
        }

        val totalPostCount = memberCount * POSTS_PER_MEMBER + (adminIds.size * 2)
        logger.info("게시글 생성 시작... 총 {} 게시글, 배치 크기: {}, 병렬 처리: {}", totalPostCount, BATCH_SIZE, PARALLEL_BATCHES)

        val prevCachingState = cacheService.enableCaching
        cacheService.enableCaching = true

        val regularUserCount = memberCount - adminIds.size
        val regularMemberIds = (1..regularUserCount).map { it.toLong() }

        val postServiceImpl = postService as? PostServiceImpl
            ?: throw IllegalStateException("PostService는 PostServiceImpl의 인스턴스여야 합니다")

        withContext(optimizedDispatcher) {
            val allMemberIds = regularMemberIds + adminIds
            postServiceImpl.preloadMemberCache(allMemberIds)
            logger.info("사용자 캐시 프리로딩 완료. 로드된 사용자 수: {}", allMemberIds.size)
        }

        val time = measureTimeMillis {
            val postBatches = generatePostBatches(regularUserCount.toInt(), adminIds)
            logger.info("게시글 배치 생성 완료. 총 {} 배치", postBatches.size)

            postBatches.chunked(PARALLEL_BATCHES).forEachIndexed { chunkIndex, batchChunk ->
                runBlocking {
                    val jobs = batchChunk.map { batch ->
                        launch(optimizedDispatcher) {
                            try {
                                postServiceImpl.createBatch(batch)
                                logger.debug("게시글 배치 처리 완료: ${batch.size}개")
                            } catch (e: Exception) {
                                logger.error("게시글 배치 처리 중 오류: ${e.message}", e)
                            }
                        }
                    }
                    jobs.joinAll()
                }

                // 메모리 관리 강화
                entityManager.clear()
                System.gc()

                val progress = min(100, ((chunkIndex + 1) * PARALLEL_BATCHES * 100) / postBatches.size)
                logger.info("게시글 생성 진행률: {}%, 처리된 배치: {}/{}", progress, (chunkIndex + 1) * PARALLEL_BATCHES, postBatches.size)
            }
        }

        cacheService.enableCaching = prevCachingState
        postServiceImpl.clearMemberCache()

        logger.info("게시글 생성 완료. 소요 시간: {}초", time / 1000)

        val finalCount = postService.count()
        logger.info("최종 등록된 게시글 수: {}", finalCount)

        return@withContext time to Result.success(Unit)
    }

    /**
     * 애플리케이션 실행 시 데이터 초기화 실행
     */
    @Bean
    fun notProdInitDataApplicationRunner(): ApplicationRunner = ApplicationRunner {
        runBlocking {
            withContext(Dispatchers.IO) {
                System.gc()
                Thread.sleep(100)
                val beforeMemory = getUsedMemoryMB()
                logger.info("데이터 초기화 시작...")

                var memberTimeActual = 0L
                var postTimeActual = 0L

                val totalTime = measureTimeMillis {
                    val (memberTime, memberResult, adminIds) = initializeMembers()
                    memberTimeActual = memberTime
                    memberResult.onSuccess {
                        logger.info("사용자 초기화 성공, 게시글 초기화 시작")
                        val (postTime, postResult) = initializePosts(adminIds)
                        postTimeActual = postTime
                        postResult.onFailure { e ->
                            logger.error("게시글 초기화 중 오류 발생: {}", e.message, e)
                        }
                    }.onFailure { e ->
                        logger.error("사용자 초기화 중 오류 발생: {}", e.message, e)
                    }
                    (postService as? PostServiceImpl)?.clearMemberCache()
                }

                System.gc()
                Thread.sleep(100)
                val afterMemory = getUsedMemoryMB()

                val metrics = PerformanceMetrics(
                    totalTimeMs = totalTime,
                    memberTimeMs = memberTimeActual,
                    postTimeMs = postTimeActual,
                    beforeMemoryMB = beforeMemory,
                    afterMemoryMB = afterMemory
                )

                logger.info(metrics.generateReport())
            }
        }
    }

    /**
     * 사용된 메모리 계산 (MB 단위)
     */
    private fun getUsedMemoryMB(): Double {
        val runtime = Runtime.getRuntime()
        val usedBytes = runtime.totalMemory() - runtime.freeMemory()
        return usedBytes.toDouble() / (1024 * 1024)
    }
}