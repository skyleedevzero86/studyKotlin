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
        private const val BATCH_SIZE = 2000 // 배치 크기 감소로 메모리 부담 완화
        private const val PARALLEL_BATCHES = 4 // 병렬 처리 단위 증가
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

    private fun generateMemberBatches(): List<List<MemberRequest>> {
        val batchCount = (MEMBER_COUNT + BATCH_SIZE - 1) / BATCH_SIZE
        return (0 until batchCount).map { batchIndex ->
            val start = batchIndex * BATCH_SIZE + 1
            val end = minOf(start + BATCH_SIZE - 1, MEMBER_COUNT)
            (start..end).map { i -> MemberRequest("user$i", "pass%02d".format(i)) }
        }
    }

    private fun generatePostBatches(memberCount: Int): List<List<PostRequest>> {
        val random = java.util.Random()
        val totalPosts = memberCount * POSTS_PER_MEMBER
        val batchCount = (totalPosts + BATCH_SIZE - 1) / BATCH_SIZE
        var postIndex = 0

        return (0 until batchCount).map {
            val batchSize = minOf(BATCH_SIZE, totalPosts - it * BATCH_SIZE)
            List(batchSize) {
                val memberId = (postIndex / POSTS_PER_MEMBER) + 1
                val title = "${sampleTitles.random()} #${UUID.randomUUID().toString().substring(0, 8)}"
                val content = "${sampleContents.random()} (작성자: user$memberId)"
                postIndex++
                PostRequest(title, content, memberId.toLong())
            }
        }
    }

    @Transactional
    suspend fun initializeMembers(): Pair<Long, InitializationResult> = withContext(Dispatchers.IO) {
        val countResult = memberService.count()
        if (countResult.isSuccess && countResult.getOrNull() ?: 0 > 0) {
            logger.info("이미 멤버가 존재합니다. 멤버 초기화를 건너뜁니다.")
            return@withContext 0L to Result.success(Unit)
        }

        logger.info("멤버 생성 시작... 총 {} 멤버, 배치 크기: {}", MEMBER_COUNT, BATCH_SIZE)
        val prevCachingState = cacheService.enableCaching
        cacheService.enableCaching = true
        cacheService.putInCache("test:key", "test-value", Duration.ofMinutes(30))

        val time = measureTimeMillis {
            val memberBatches = generateMemberBatches()
            memberBatches.chunked(PARALLEL_BATCHES).forEachIndexed { chunkIndex, batchChunk ->
                batchChunk.map { batch ->
                    async {
                        (memberService as? MemberServiceImpl)?.registerBatch(batch)
                            ?: throw IllegalStateException("MemberService는 MemberServiceImpl의 인스턴스여야 합니다")
                    }
                }.awaitAll()
                // 배치마다 EntityManager 클리어로 메모리 최적화
                if (chunkIndex % 2 == 0) {
                    entityManager.clear()
                    System.gc()
                }
            }
        }

        cacheService.enableCaching = prevCachingState
        logger.info("멤버 생성 완료. 소요 시간: {}초", time / 1000)
        return@withContext time to Result.success(Unit)
    }

    @Transactional
    suspend fun initializePosts(): Pair<Long, InitializationResult> = withContext(Dispatchers.IO) {
        val count = postService.count()
        if (count > 0) {
            logger.info("이미 게시글이 존재합니다. 게시글 초기화를 건너뜁니다.")
            return@withContext 0L to Result.success(Unit)
        }

        logger.info("게시글 생성 시작... 총 {} 게시글, 배치 크기: {}", MEMBER_COUNT * POSTS_PER_MEMBER, BATCH_SIZE)
        val prevCachingState = cacheService.enableCaching
        cacheService.enableCaching = true
        cacheService.putInCache("test:key", "test-value", Duration.ofMinutes(30))

        val memberIds = (1..MEMBER_COUNT).map { it.toLong() }
        (postService as? PostServiceImpl)?.preloadMemberCache(memberIds)

        val time = measureTimeMillis {
            val postBatches = generatePostBatches(MEMBER_COUNT)
            postBatches.chunked(PARALLEL_BATCHES).forEachIndexed { chunkIndex, batchChunk ->
                batchChunk.map { batch ->
                    async {
                        (postService as? PostServiceImpl)?.createBatch(batch)
                            ?: throw IllegalStateException("PostService는 PostServiceImpl의 인스턴스여야 합니다")
                    }
                }.awaitAll()
                // 배치마다 EntityManager 클리어로 메모리 최적화
                if (chunkIndex % 2 == 0) {
                    entityManager.clear()
                    System.gc()
                }
            }
        }

        cacheService.enableCaching = prevCachingState
        (postService as? PostServiceImpl)?.clearMemberCache()
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