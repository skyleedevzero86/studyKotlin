package com.komroonga.member.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.komroonga.domain.member.dto.*
import com.komroonga.global.error.types.MemberError
import com.komroonga.global.utils.CacheService
import com.komroonga.global.utils.withLogging
import com.komroonga.member.entity.Member
import com.komroonga.member.repository.MemberRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.slf4j.Logger
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

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    private val cacheTtl = Duration.ofMinutes(30)
    private val memberDispatcher = Dispatchers.IO.limitedParallelism(10)

    /**
     * 회원 등록
     */
    override suspend fun register(request: MemberRequest): MemberResult<MemberResponse> =
        validateRequest(request)
            .map { req ->
                memberRepository.findByUsername(req.username)
                    ?.let { throw MemberError.AlreadyExists(req.username) }
                Member(
                    username = req.username,
                    password = passwordEncoder.encode(req.password),
                    name = req.name,
                    email = req.email,
                    role = req.role
                )
            }
            .map { memberRepository.save(it) }
            .map { saved ->
                if (cacheService.enableCaching) {
                    cacheService.putInCache(
                        "member:dto:${saved.id}",
                        MemberResponse(saved.id!!, saved.username, saved.name, saved.email, saved.role),
                        cacheTtl
                    )
                }
                MemberResponse(saved.id!!, saved.username, saved.name, saved.email, saved.role)
            }
            .withLogging(logger, "회원 등록")

    /**
     * 배치 회원 등록
     */
    @Transactional
    override suspend fun registerBatch(requests: List<MemberRequest>): List<MemberResponse> =
        requests.map { validateRequest(it).getOrThrow() }
            .map { req ->
                Member(
                    username = req.username,
                    password = passwordEncoder.encode(req.password),
                    name = req.name.ifBlank { "" },
                    email = req.email.ifBlank { "" },
                    role = req.role
                )
            }
            .let { memberRepository.saveAll(it) }
            .map { saved ->
                MemberResponse(saved.id!!, saved.username, saved.name, saved.email, saved.role)
            }
            .also { responses ->
                if (cacheService.enableCaching) {
                    cacheService.putBulkInCache(responses.associate { it.id.toString() to it }, "member:dto", cacheTtl)
                }
            }

    /**
     * 사용자 이름으로 회원 조회
     */
    override suspend fun findByUsername(username: String): MemberResult<MemberResponse> =
        cacheService.getCachedOrCompute("member:username:$username", cacheTtl) {
            memberRepository.findByUsername(username)
                ?.let { MemberResponse(it.id!!, it.username, it.name, it.email, it.role) }
                ?: throw MemberError.NotFound(username)
        }.withLogging(logger, "사용자 이름으로 회원 조회")

    /**
     * 모든 회원 조회
     */
    override suspend fun findAll(): Flow<MemberResponse> = flow {
        memberRepository.findAll()
            .map { MemberResponse(it.id!!, it.username, it.name, it.email, it.role) }
            .forEach { emit(it) }
    }

    /**
     * 회원 수 조회
     */
    override suspend fun count(): MemberResult<Long> =
        runCatching { memberRepository.count() }
            .withLogging(logger, "회원 수 조회")

    /**
     * 사용자 이름으로 회원 엔티티 조회
     */
    override suspend fun findMemberEntityByUsername(username: String): Result<Member> =
        runCatching {
            memberRepository.findByUsername(username) ?: throw MemberError.NotFound(username)
        }.withLogging(logger, "사용자 이름으로 회원 엔티티 조회")

    /**
     * ID로 회원 엔티티 조회
     */
    override suspend fun findMemberEntityById(id: Long): Result<Member> =
        runCatching {
            if (cacheService.enableCaching && cacheService.exists("member:id:$id")) {
                memberRepository.findById(id).orElseThrow { MemberError.NotFound("ID: $id") }
            } else {
                memberRepository.findById(id).orElseThrow { MemberError.NotFound("ID: $id") }
                    .also { if (cacheService.enableCaching) cacheService.putInCache("member:id:$id", true, cacheTtl) }
            }
        }.withLogging(logger, "ID로 회원 엔티티 조회")

    /**
     * 여러 ID로 회원 조회
     */
    override suspend fun findAllByIds(ids: List<Long>): List<Member> =
        withContext(memberDispatcher) {
            memberRepository.findAllById(ids).toList()
        }

    /**
     * 회원 요청 유효성 검사
     */
    private fun validateRequest(request: MemberRequest): Result<MemberRequest> = when {
        request.username.isBlank() -> Result.failure(MemberError.InvalidInput("username", "사용자 이름은 비어 있을 수 없습니다"))
        request.password.length < 6 -> Result.failure(MemberError.InvalidInput("password", "비밀번호는 6자 이상이어야 합니다"))
        else -> Result.success(request)
    }

    /**
     * 키워드로 회원 검색
     */
    override suspend fun searchByKeyword(keyword: String): MemberResult<List<MemberResponse>> =
        withContext(memberDispatcher) {
            runCatching {
                memberRepository.searchByKeyword(keyword)
                    .map { MemberResponse(it.id!!, it.username, it.name, it.email, it.role) }
            }
        }.withLogging(logger, "키워드로 회원 검색")
}