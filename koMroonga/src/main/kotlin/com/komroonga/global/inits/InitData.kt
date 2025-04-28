package com.komroonga.global.inits

import com.komroonga.domain.member.dto.MemberRequest
import com.komroonga.domain.post.dto.PostRequest
import com.komroonga.domain.post.service.PostService
import com.komroonga.domain.post.service.PostServiceImpl
import com.komroonga.global.utils.CacheService
import com.komroonga.global.utils.InitializationResult
import com.komroonga.member.service.MemberService
import com.komroonga.member.service.MemberServiceImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.system.measureTimeMillis

@Configuration
class InitData(
    private val memberService: MemberService,
    private val postService: PostService,
    private val cacheService: CacheService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        private const val MEMBER_COUNT = 100_000
        private const val POSTS_PER_MEMBER = 1
        private const val BATCH_SIZE = 500 // 배치 크기 감소
        private const val PARALLEL_BATCHES = 4 // 병렬 처리 수
    }

    data class PostData(val title: String, val content: String)

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
        val batches = mutableListOf<List<MemberRequest>>()

        for (batchIndex in 0 until batchCount) {
            val start = batchIndex * BATCH_SIZE + 1
            val end = minOf(start + BATCH_SIZE - 1, MEMBER_COUNT)

            batches.add((start..end).map { i ->
                MemberRequest("user$i", "pass%02d".format(i))
            })
        }

        return batches
    }

    private fun generatePostBatches(memberCount: Int): List<List<PostRequest>> {
        val random = java.util.Random()
        val totalPosts = memberCount * POSTS_PER_MEMBER
        val batchCount = (totalPosts + BATCH_SIZE - 1) / BATCH_SIZE
        val batches = mutableListOf<List<PostRequest>>()

        var postIndex = 0
        for (batchIndex in 0 until batchCount) {
            val batchRequests = mutableListOf<PostRequest>()
            val batchSize = minOf(BATCH_SIZE, totalPosts - batchIndex * BATCH_SIZE)

            for (i in 0 until batchSize) {
                val memberId = (postIndex / POSTS_PER_MEMBER) + 1
                val titleIdx = random.nextInt(sampleTitles.size)
                val contentIdx = random.nextInt(sampleContents.size)
                val uniqueId = UUID.randomUUID().toString().substring(0, 8)
                val title = "${sampleTitles[titleIdx]} #$uniqueId"
                val content = "${sampleContents[contentIdx]} (작성자: user$memberId, 번호: ${postIndex % POSTS_PER_MEMBER + 1})"

                batchRequests.add(PostRequest(title, content, memberId.toLong()))
                postIndex++
            }

            batches.add(batchRequests)
        }

        return batches
    }

    private suspend fun initializeMembers(): InitializationResult = withContext(Dispatchers.IO) {
        val customDispatcher = Dispatchers.IO.limitedParallelism(PARALLEL_BATCHES)

        val countResult = memberService.count()
        if (countResult.isSuccess && countResult.getOrNull() ?: 0 > 0) {
            logger.info("이미 멤버가 존재합니다. 멤버 초기화를 건너뜁니다.")
            return@withContext Result.success(Unit)
        }

        logger.info("멤버 생성 시작... 총 {} 멤버, 배치 크기: {}", MEMBER_COUNT, BATCH_SIZE)

        // 초기화 중에는 캐싱 비활성화
        val prevCachingState = cacheService.enableCaching
        cacheService.enableCaching = false

        val time = measureTimeMillis {
            val memberBatches = generateMemberBatches()

            coroutineScope {
                memberBatches.chunked(PARALLEL_BATCHES).forEach { batchChunk ->
                    val tasks = batchChunk.map { batch ->
                        async(customDispatcher) {
                            executeInNewTransaction {
                                (memberService as? MemberServiceImpl)?.registerBatch(batch)
                                    ?: throw IllegalStateException("MemberService는 MemberServiceImpl의 인스턴스여야 합니다")
                            }
                            logger.info("프로세스 배치: {} 멤버", batch.size)
                        }
                    }
                    tasks.awaitAll()
                }
            }
        }

        // 캐싱 상태 복원
        cacheService.enableCaching = prevCachingState

        logger.info("멤버 생성 완료. 소요 시간: {}초", time / 1000)
        Result.success(Unit)
    }

    private suspend fun initializePosts(): InitializationResult = withContext(Dispatchers.IO) {
        val customDispatcher = Dispatchers.IO.limitedParallelism(PARALLEL_BATCHES)

        val count = postService.count()
        if (count > 0) {
            logger.info("이미 게시글이 존재합니다. 게시글 초기화를 건너뜁니다.")
            return@withContext Result.success(Unit)
        }

        logger.info("게시글 생성 시작... 총 {} 게시글, 배치 크기: {}", MEMBER_COUNT * POSTS_PER_MEMBER, BATCH_SIZE)

        // 초기화 중에는 캐싱 비활성화
        val prevCachingState = cacheService.enableCaching
        cacheService.enableCaching = false

        // 포스트 서비스 멤버 캐시 초기화
        (postService as? PostServiceImpl)?.clearMemberCache()

        val time = measureTimeMillis {
            val postBatches = generatePostBatches(MEMBER_COUNT)

            coroutineScope {
                postBatches.chunked(PARALLEL_BATCHES).forEach { batchChunk ->
                    val tasks = batchChunk.map { batch ->
                        async(customDispatcher) {
                            executeInNewTransaction {
                                (postService as? PostServiceImpl)?.createBatch(batch)
                                    ?: throw IllegalStateException("PostService는 PostServiceImpl의 인스턴스여야 합니다")
                            }
                            logger.info("프로세스 배치: {} 게시글", batch.size)
                        }
                    }
                    tasks.awaitAll()
                }
            }
        }

        // 캐싱 상태 복원
        cacheService.enableCaching = prevCachingState

        logger.info("게시글 생성 완료. 소요 시간: {}초", time / 1000)
        Result.success(Unit)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    suspend fun <T> executeInNewTransaction(block: suspend () -> T): T = block()

    private fun interface DataInitializer {
        suspend fun initialize(): InitializationResult
    }

    private val memberInitializer = DataInitializer { initializeMembers() }
    private val postInitializer = DataInitializer { initializePosts() }

    @Bean
    fun notProdInitDataApplicationRunner(): ApplicationRunner = ApplicationRunner {
        runBlocking {
            withContext(Dispatchers.IO) {
                logger.info("데이터 초기화 시작...")
                val totalTime = measureTimeMillis {
                    // 초기화 중에는 캐싱 비활성화
                    val prevCachingState = cacheService.enableCaching
                    cacheService.enableCaching = false

                    try {
                        memberInitializer.initialize()
                            .onSuccess {
                                logger.info("멤버 초기화 성공, 게시글 초기화 시작")
                                postInitializer.initialize()
                                    .onFailure { e ->
                                        logger.error("게시글 초기화 중 오류 발생: {}", e.message, e)
                                    }
                            }
                            .onFailure { e ->
                                logger.error("멤버 초기화 중 오류 발생: {}", e.message, e)
                            }
                    } finally {
                        // 캐싱 상태 복원
                        cacheService.enableCaching = prevCachingState
                    }
                }
                logger.info("모든 데이터 초기화 완료. 총 소요 시간: {}초", totalTime / 1000)
            }
        }
    }
}