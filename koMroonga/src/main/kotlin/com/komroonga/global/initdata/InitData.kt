package com.komroonga.global.initdata

import com.komroonga.domain.member.dto.MemberRequest
import com.komroonga.domain.post.service.PostService
import com.komroonga.global.utils.InitializationResult
import com.komroonga.member.service.MemberService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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
    companion object {
        private const val MEMBER_COUNT = 1_000_000
        private const val POSTS_PER_MEMBER = 1  // 각 멤버당 게시물 수 (총 100만 게시물)
        private const val BATCH_SIZE = 1000     // 트랜잭션당 처리할 항목 수
    }

    // 불변 데이터 클래스 정의
    data class PostData(val title: String, val content: String)

    // 샘플 제목과 내용 리스트
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

    // 멤버 데이터 생성을 위한 시퀀스 생성 함수
    private fun generateMemberSequence(): Sequence<MemberRequest> = sequence {
        for (i in 1..MEMBER_COUNT) {
            val username = "user$i"
            val password = "pass$i"
            yield(MemberRequest(username, password))
        }
    }

    // 게시글 데이터 생성을 위한 시퀀스 생성 함수
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

    // 멤버 초기화 함수
    private suspend fun initializeMembers(): InitializationResult = runCatching {
        val count = memberService.count().getOrThrow()
        if (count > 0) {
            println("이미 멤버가 존재합니다. 멤버 초기화를 건너뜁니다.")
            return@runCatching
        }

        println("멤버 생성 시작... 총 $MEMBER_COUNT 멤버")
        val time = measureTimeMillis {
            // 시퀀스를 청크로 나누어 배치 처리
            generateMemberSequence()
                .chunked(BATCH_SIZE)
                .forEachIndexed { index, batchMembers ->
                    executeInNewTransaction {
                        runBlocking {
                            batchMembers.forEach { request ->
                                memberService.register(request)
                            }
                        }
                    }

                    if ((index + 1) % 100 == 0) {
                        println("멤버 생성 진행 중: ${(index + 1) * BATCH_SIZE}/$MEMBER_COUNT")
                    }
                }
        }
        println("멤버 생성 완료. 소요 시간: ${time / 1000}초")
    }


    // 게시글 초기화 함수
    private suspend fun initializePosts(): InitializationResult = runCatching {
        if (postService.count() > 0) {
            println("이미 게시글이 존재합니다. 게시글 초기화를 건너뜁니다.")
            return@runCatching
        }

        println("게시글 생성 시작... 총 ${MEMBER_COUNT * POSTS_PER_MEMBER} 게시글")
        val time = measureTimeMillis {
            // 시퀀스를 청크로 나누어 배치 처리
            generatePostSequence(MEMBER_COUNT)
                .chunked(BATCH_SIZE)
                .forEachIndexed { index, batchPosts ->
                    executeInNewTransaction {
                        runBlocking {
                            batchPosts.forEach { (memberId, postData) ->
                                val member = withContext(Dispatchers.IO) {
                                    memberService.findByUsername("user$memberId").getOrThrow()
                                }
                                postService.edit(member, postData.title, postData.content)
                            }
                        }
                    }

                    if ((index + 1) % 100 == 0) {
                        println("게시글 생성 진행 중: ${(index + 1) * BATCH_SIZE}/${MEMBER_COUNT * POSTS_PER_MEMBER}")
                    }
                }
        }
        println("게시글 생성 완료. 소요 시간: ${time / 1000}초")
    }

    // 트랜잭션 분리를 위한 함수
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun executeInNewTransaction(block: () -> Unit) {
        block()
    }

    // 트랜잭션 처리를 분리하여 함수형 인터페이스로 정의
    private fun interface DataInitializer {
        suspend fun initialize(): InitializationResult
    }

    private val memberInitializer = DataInitializer {
        initializeMembers()
    }

    private val postInitializer = DataInitializer {
        initializePosts()
    }

    @Bean
    fun notProdInitDataApplicationRunner(): ApplicationRunner {
        return ApplicationRunner {
            runBlocking {
                withContext(Dispatchers.IO) {
                    println("데이터 초기화 시작...")
                    val totalTime = measureTimeMillis {
                        // 멤버 생성 후 게시글 생성
                        memberInitializer.initialize()
                            .onSuccess { postInitializer.initialize() }
                            .onFailure { exception ->
                                println("초기화 중 오류 발생: ${exception.message}")
                                exception.printStackTrace()
                            }
                    }
                    println("모든 데이터 초기화 완료. 총 소요 시간: ${totalTime / 1000}초")
                }
            }
        }
    }
}