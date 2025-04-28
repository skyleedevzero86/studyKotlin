package com.komroonga.global.inits

import com.komroonga.domain.member.dto.MemberRequest
import com.komroonga.domain.post.dto.PostRequest
import com.komroonga.domain.post.service.PostService
import com.komroonga.global.utils.InitializationResult
import com.komroonga.member.service.MemberService
import kotlinx.coroutines.Dispatchers
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
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        private const val MEMBER_COUNT = 100_000
        private const val POSTS_PER_MEMBER = 1
        private const val BATCH_SIZE = 1000
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

    private fun generateMemberSequence(): Sequence<MemberRequest> = sequence {
        for (i in 1..MEMBER_COUNT) {
            yield(MemberRequest("user$i", "pass%02d".format(i)))
        }
    }

    private fun generatePostSequence(memberCount: Int): Sequence<Pair<Int, PostData>> = sequence {
        val random = java.util.Random()
        for (memberId in 1..memberCount) {
            for (postIdx in 1..POSTS_PER_MEMBER) {
                val titleIdx = random.nextInt(sampleTitles.size)
                val contentIdx = random.nextInt(sampleContents.size)
                val uniqueId = UUID.randomUUID().toString().substring(0, 8)
                val title = "${sampleTitles[titleIdx]} #$uniqueId"
                val content = "${sampleContents[contentIdx]} (작성자: user$memberId, 번호: $postIdx)"
                yield(memberId to PostData(title, content))
            }
        }
    }

    private suspend fun initializeMembers(): InitializationResult = withContext(Dispatchers.IO) {
        val countResult = memberService.count()
        if (countResult.isSuccess && countResult.getOrNull() ?: 0 > 0) {
            logger.info("이미 멤버가 존재합니다. 멤버 초기화를 건너뜁니다.")
            return@withContext Result.success(Unit)
        }

        logger.info("멤버 생성 시작... 총 {} 멤버", MEMBER_COUNT)
        val time = measureTimeMillis {
            generateMemberSequence()
                .asFlow()
                .buffer(BATCH_SIZE)
                .map { request ->
                    executeInNewTransaction {
                        val registerResult = memberService.register(request)
                        if (registerResult.isFailure) {
                            logger.error("멤버 등록 실패: {}", request.username, registerResult.exceptionOrNull())
                            throw registerResult.exceptionOrNull() ?: Exception("Unknown error")
                        }
                        logger.debug("Processing member: {}", request.username)
                    }
                }
                .collect { it }
        }
        logger.info("멤버 생성 완료. 소요 시간: {}초", time / 1000)
        Result.success(Unit)
    }

    private suspend fun initializePosts(): InitializationResult = withContext(Dispatchers.IO) {
        val count = postService.count()
        if (count > 0) {
            logger.info("이미 게시글이 존재합니다. 게시글 초기화를 건너뜁니다.")
            return@withContext Result.success(Unit)
        }

        logger.info("게시글 생성 시작... 총 {} 게시글", MEMBER_COUNT * POSTS_PER_MEMBER)
        val time = measureTimeMillis {
            generatePostSequence(MEMBER_COUNT)
                .asFlow()
                .buffer(BATCH_SIZE)
                .map { (memberId, postData) ->
                    executeInNewTransaction {
                        val memberResult = memberService.findMemberEntityByUsername("user$memberId")
                        if (memberResult.isFailure) {
                            logger.error("회원 조회 실패 for user{}: {}", memberId, memberResult.exceptionOrNull()?.message)
                            throw memberResult.exceptionOrNull() ?: Exception("Unknown error")
                        }
                        val member = memberResult.getOrThrow()
                        val request = PostRequest(postData.title, postData.content, member.id)
                        val createResult = postService.create(request)
                        if (createResult.isFailure) {
                            logger.error("게시글 생성 실패 for user{}: {}", memberId, createResult.exceptionOrNull()?.message)
                            throw createResult.exceptionOrNull() ?: Exception("Unknown error")
                        }
                        logger.debug("Created post for user{}: title={}", memberId, postData.title)
                    }
                }
                .collect { it }
        }
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
                    memberInitializer.initialize()
                        .onSuccess { postInitializer.initialize() }
                        .onFailure { e ->
                            logger.error("초기화 중 오류 발생: {}", e.message, e)
                        }
                }
                logger.info("모든 데이터 초기화 완료. 총 소요 시간: {}초", totalTime / 1000)
            }
        }
    }
}