package com.komroonga.member.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.komroonga.domain.member.dto.*
import com.komroonga.global.error.types.MemberError
import com.komroonga.global.utils.CacheService
import com.komroonga.member.entity.Member
import com.komroonga.member.repository.MemberRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

/**
 * 회원 서비스 구현 클래스
 * 회원 관련 비즈니스 로직 처리
 */
@Service
class MemberServiceImpl(
    private val memberRepository: MemberRepository,
    private val cacheService: CacheService,
    private val passwordEncoder: PasswordEncoder,
    @Qualifier("redisObjectMapper") private val objectMapper: ObjectMapper
) : MemberService {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val cacheTtl = Duration.ofMinutes(30)
    private val memberDispatcher = Dispatchers.IO.limitedParallelism(10)

    /**
     * 회원 등록
     */
    override suspend fun register(request: MemberRequest): MemberResult<MemberResponse> =
        runCatching {
            logger.info("사용자 등록: ${request.username}")
            validateRequest(request)
            memberRepository.findByUsername(request.username)?.let {
                logger.warn("사용자 이름 ${request.username} 이미 존재")
                throw MemberError.AlreadyExists(request.username)
            }
            val member = Member(
                username = request.username,
                password = passwordEncoder.encode(request.password),
                name = request.name,
                email = request.email,
                role = request.role
            )
            val saved = memberRepository.save(member)
            logger.info("사용자 저장 완료: ${saved.username}")

            if (cacheService.enableCaching) {
                cacheService.putInCache("member:dto:${saved.id}", MemberResponse(saved.id!!, saved.username, saved.name, saved.email, saved.role), cacheTtl)
            }
            MemberResponse(saved.id!!, saved.username, saved.name, saved.email, saved.role)
        }.onFailure {
            logger.error("회원 등록 실패: ${request.username}, 오류: ${it.message}", it)
        }

    /**
     * 배치 회원 등록
     */
    @Transactional
    override suspend fun registerBatch(requests: List<MemberRequest>): List<MemberResponse> {
        val members = requests.map { request ->
            validateRequest(request)
            Member(
                username = request.username,
                password = passwordEncoder.encode(request.password),
                name = request.name.ifBlank { "" },
                email = request.email.ifBlank { "" },
                role = request.role
            )
        }
        val savedMembers = memberRepository.saveAll(members)
        val responses = savedMembers.map { MemberResponse(it.id!!, it.username, it.name, it.email, it.role) }

        if (cacheService.enableCaching) {
            val cacheItems = responses.associate { it.id.toString() to it }
            cacheService.putBulkInCache(cacheItems, "member:dto", cacheTtl)
        }
        return responses
    }

    /**
     * 사용자 이름으로 회원 조회
     */
    override suspend fun findByUsername(username: String): MemberResult<MemberResponse> =
        runCatching {
            cacheService.getCachedOrCompute(
                key = "member:username:$username",
                ttl = cacheTtl
            ) {
                val member = memberRepository.findByUsername(username)
                    ?: throw MemberError.NotFound(username)
                MemberResponse(member.id!!, member.username, member.name, member.email, member.role)
            }.getOrThrow()
        }.onFailure { logger.error("회원 조회 실패: ${it.message}", it) }

    /**
     * 모든 회원 조회
     */
    override suspend fun findAll(): Flow<MemberResponse> = flow {
        memberRepository.findAll()
            .map { MemberResponse(it.id!!, it.username, it.name, it.email, it.role) }
            .onEach { logger.info("전체 회원 조회 완료") }
            .forEach { emit(it) }
    }

    /**
     * 회원 수 조회
     */
    override suspend fun count(): MemberResult<Long> =
        runCatching {
            memberRepository.count()
        }.onFailure { logger.error("회원 수 조회 실패: ${it.message}", it) }

    /**
     * 사용자 이름으로 회원 엔티티 조회
     */
    override suspend fun findMemberEntityByUsername(username: String): Result<Member> =
        runCatching {
            val member = memberRepository.findByUsername(username)
                ?: throw MemberError.NotFound(username)
            member
        }.onFailure { logger.error("회원 엔티티 조회 실패: ${it.message}", it) }

    /**
     * ID로 회원 엔티티 조회
     * 수정: Member 엔티티 대신 ID만 캐싱하여 ClassCastException 방지
     */
    override suspend fun findMemberEntityById(id: Long): Result<Member> =
        runCatching {
            // 엔티티 자체가 아닌 ID만 캐시에 저장하고, 해당 ID로 항상 DB 조회
            // 캐시 키를 통해 해당 ID의 회원이 존재하는지만 확인
            if (cacheService.enableCaching && cacheService.exists("member:id:$id")) {
                // 캐시에 ID가 존재하면 바로 DB 조회
                memberRepository.findById(id)
                    .orElseThrow { MemberError.NotFound("ID: $id") }
            } else {
                // 캐시에 ID가 없으면 DB 조회 후 ID 정보만 캐시에 저장
                val member = memberRepository.findById(id)
                    .orElseThrow { MemberError.NotFound("ID: $id") }

                if (cacheService.enableCaching) {
                    // ID 정보만 캐시에 저장 (값은 단순히 true로 저장)
                    cacheService.putInCache("member:id:$id", true, cacheTtl)
                }
                member
            }
        }.onFailure {
            logger.error("회원 엔티티 조회 실패 (ID: $id): ${it.message}", it)
        }

    /**
     * 회원 요청 유효성 검사
     */
    private fun validateRequest(request: MemberRequest) {
        if (request.username.isBlank()) {
            throw MemberError.InvalidInput("username", "사용자 이름은 비어 있을 수 없습니다")
        }
        if (request.password.length < 6) {
            throw MemberError.InvalidInput("password", "비밀번호는 6자 이상이어야 합니다")
        }
    }

    /**
     * 키워드로 회원 검색
     */
    override suspend fun searchByKeyword(keyword: String): MemberResult<List<MemberResponse>> =
        runCatching {
            withContext(memberDispatcher) {
                val members = memberRepository.searchByKeyword(keyword)
                members.map { MemberResponse(it.id!!, it.username, it.name, it.email, it.role) }
            }
        }.onFailure { logger.error("회원 검색 실패: ${it.message}", it) }
}