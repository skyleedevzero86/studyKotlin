package com.komroonga.global.inits

import com.komroonga.domain.member.dto.MemberRequest
import com.komroonga.domain.post.dto.PostRequest
import com.komroonga.domain.post.service.PostService
import com.komroonga.domain.post.service.PostServiceImpl
import com.komroonga.global.utils.CacheService
import com.komroonga.global.utils.InitializationResult
import com.komroonga.global.utils.PerformanceMetrics
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
import kotlin.system.measureTimeMillis
import java.time.Duration

@Configuration
class InitData(
    private val memberService: MemberService,
    private val postService: PostService,
    private val cacheService: CacheService,
    @PersistenceContext private val entityManager: EntityManager
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        private const val MEMBER_COUNT = 100_000
        private const val POSTS_PER_MEMBER = 1
        private const val BATCH_SIZE = 5000 // 배치 크기 증가로 처리량 향상
        private const val PARALLEL_BATCHES = 8 // 병렬 처리 단위 증가

        // 최적화된 코루틴 디스패처 설정
        private val optimizedDispatcher = Dispatchers.IO.limitedParallelism(16)

        // 대량 데이터 처리용 고정 스레드 풀 디스패처
        private val bulkProcessingDispatcher = Executors.newFixedThreadPool(32)
            .asCoroutineDispatcher()
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

    // 함수형 스타일로 멤버 배치 생성 최적화
    private fun generateMemberBatches(): Sequence<List<MemberRequest>> = sequence {
        val batchCount = (MEMBER_COUNT + BATCH_SIZE - 1) / BATCH_SIZE

        // 시퀀스를 사용하여 메모리 효율성 향상
        (0 until batchCount).forEach { batchIndex ->
            val start = batchIndex * BATCH_SIZE + 1
            val end = minOf(start + BATCH_SIZE - 1, MEMBER_COUNT)

            // 배치 단위로 생성하여 메모리 사용량 최적화
            val batch = (start..end).map { i -> 
                MemberRequest("user$i", "pass%02d".format(i))
            }

            yield(batch)
        }
    }

    // 함수형 스타일로 게시글 배치 생성 최적화
    private fun generatePostBatches(memberCount: Int): Sequence<List<PostRequest>> = sequence {
        val random = java.util.Random()
        val totalPosts = memberCount * POSTS_PER_MEMBER
        val batchCount = (totalPosts + BATCH_SIZE - 1) / BATCH_SIZE

        // 시퀀스와 원자적 카운터를 사용하여 스레드 안전성 확보
        val postIndexCounter = java.util.concurrent.atomic.AtomicInteger(0)

        // 각 배치를 독립적으로 생성하여 병렬 처리 가능하게 함
        (0 until batchCount).forEach { batchIndex ->
            val batchSize = minOf(BATCH_SIZE, totalPosts - batchIndex * BATCH_SIZE)

            val batch = (0 until batchSize).map {
                val postIndex = postIndexCounter.getAndIncrement()
                val memberId = (postIndex / POSTS_PER_MEMBER) + 1

                // UUID 생성 최적화
                val uniqueId = UUID.randomUUID().toString().substring(0, 8)
                val title = "${sampleTitles.random()} #$uniqueId"
                val content = "${sampleContents.random()} (작성자: user$memberId)"

                PostRequest(title, content, memberId.toLong())
            }

            yield(batch)
        }
    }

    /**
     * 멤버 초기화를 함수형 프로그래밍 스타일로 최적화
     * - 병렬 처리 향상
     * - 메모리 사용량 최적화
     * - 불필요한 GC 호출 제거
     */
    @Transactional
    suspend fun initializeMembers(): Pair<Long, InitializationResult> = withContext(bulkProcessingDispatcher) {
        // 기존 멤버 확인 - 함수형 스타일로 변환
        memberService.count()
            .takeIf { it.isSuccess && (it.getOrNull() ?: 0) > 0 }
            ?.let {
                logger.info("이미 멤버가 존재합니다. 멤버 초기화를 건너뜁니다.")
                return@withContext 0L to Result.success(Unit)
            }

        logger.info("멤버 생성 시작... 총 {} 멤버, 배치 크기: {}, 병렬 처리: {}", 
            MEMBER_COUNT, BATCH_SIZE, PARALLEL_BATCHES)

        // 캐싱 설정 최적화
        val prevCachingState = cacheService.enableCaching
        cacheService.enableCaching = true

        val time = measureTimeMillis {
            // 시퀀스 기반 배치 처리로 메모리 효율성 향상
            generateMemberBatches()
                .chunked(PARALLEL_BATCHES)
                .forEachIndexed { chunkIndex, batchChunk ->
                    // 코루틴 스코프 내에서 병렬 처리
                    coroutineScope {
                        val deferreds = batchChunk.map { batch ->
                            async(optimizedDispatcher) {
                                (memberService as? MemberServiceImpl)?.registerBatch(batch)
                                    ?: throw IllegalStateException("MemberService는 MemberServiceImpl의 인스턴스여야 합니다")
                            }
                        }
                        deferreds.awaitAll()
                    }

                    // 메모리 관리 최적화 - 불필요한 GC 호출 제거
                    if (chunkIndex % 4 == 0) {
                        entityManager.clear()
                    }

                    // 진행 상황 로깅
                    val progress = minOf(100, ((chunkIndex + 1) * PARALLEL_BATCHES * 100) / 
                        ((MEMBER_COUNT + BATCH_SIZE - 1) / BATCH_SIZE))
                    logger.info("멤버 생성 진행률: {}%", progress)
                }
        }

        cacheService.enableCaching = prevCachingState
        logger.info("멤버 생성 완료. 소요 시간: {}초", time / 1000)
        return@withContext time to Result.success(Unit)
    }

    /**
     * 게시글 초기화를 함수형 프로그래밍 스타일로 최적화
     * - 병렬 처리 향상
     * - 메모리 사용량 최적화
     * - 캐시 활용 극대화
     */
    @Transactional
    suspend fun initializePosts(): Pair<Long, InitializationResult> = withContext(bulkProcessingDispatcher) {
        // 함수형 스타일로 기존 게시글 확인
        postService.count().takeIf { it > 0 }?.let {
            logger.info("이미 게시글이 존재합니다. 게시글 초기화를 건너뜁니다.")
            return@withContext 0L to Result.success(Unit)
        }

        logger.info("게시글 생성 시작... 총 {} 게시글, 배치 크기: {}, 병렬 처리: {}", 
            MEMBER_COUNT * POSTS_PER_MEMBER, BATCH_SIZE, PARALLEL_BATCHES)

        // 캐싱 최적화
        val prevCachingState = cacheService.enableCaching
        cacheService.enableCaching = true

        // 멤버 캐시 최적화 - 병렬로 멤버 데이터 미리 로드
        val memberIds = (1..MEMBER_COUNT).map { it.toLong() }
        val postServiceImpl = postService as? PostServiceImpl 
            ?: throw IllegalStateException("PostService는 PostServiceImpl의 인스턴스여야 합니다")

        // 병렬 캐시 프리로딩
        withContext(optimizedDispatcher) {
            postServiceImpl.preloadMemberCache(memberIds)
        }

        val time = measureTimeMillis {
            // 시퀀스 기반 배치 처리로 메모리 효율성 향상
            generatePostBatches(MEMBER_COUNT)
                .chunked(PARALLEL_BATCHES)
                .forEachIndexed { chunkIndex, batchChunk ->
                    // 코루틴 스코프 내에서 병렬 처리
                    coroutineScope {
                        val deferreds = batchChunk.map { batch ->
                            async(optimizedDispatcher) {
                                postServiceImpl.createBatch(batch)
                            }
                        }
                        deferreds.awaitAll()
                    }

                    // 메모리 관리 최적화 - 불필요한 GC 호출 제거
                    if (chunkIndex % 4 == 0) {
                        entityManager.clear()
                    }

                    // 진행 상황 로깅
                    val totalBatches = (MEMBER_COUNT * POSTS_PER_MEMBER + BATCH_SIZE - 1) / BATCH_SIZE
                    val progress = minOf(100, ((chunkIndex + 1) * PARALLEL_BATCHES * 100) / totalBatches)
                    logger.info("게시글 생성 진행률: {}%", progress)
                }
        }

        // 리소스 정리
        cacheService.enableCaching = prevCachingState
        postServiceImpl.clearMemberCache()

        logger.info("게시글 생성 완료. 소요 시간: {}초", time / 1000)
        return@withContext time to Result.success(Unit)
    }

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
                    val (memberTime, memberResult) = initializeMembers()
                    memberTimeActual = memberTime
                    memberResult.onSuccess {
                        logger.info("멤버 초기화 성공, 게시글 초기화 시작")
                        val (postTime, postResult) = initializePosts()
                        postTimeActual = postTime
                        postResult.onFailure { e ->
                            logger.error("게시글 초기화 중 오류 발생: {}", e.message, e)
                        }
                    }.onFailure { e ->
                        logger.error("멤버 초기화 중 오류 발생: {}", e.message, e)
                    }
                    (postService as? PostServiceImpl)?.clearMemberCache()
                }

                System.gc()
                Thread.sleep(100)
                val afterMemory = getUsedMemoryMB()

                // 성능 지표 계산
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

    fun getUsedMemoryMB(): Double {
        val runtime = Runtime.getRuntime()
        val usedBytes = runtime.totalMemory() - runtime.freeMemory()
        return usedBytes.toDouble() / (1024 * 1024)
    }
}
