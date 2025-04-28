package com.komroonga.member.service

import com.komroonga.domain.member.dto.*
import com.komroonga.global.error.types.MemberError
import com.komroonga.global.utils.CacheService
import com.komroonga.member.entity.Member
import com.komroonga.member.repository.MemberRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class MemberServiceImpl(
    private val memberRepository: MemberRepository,
    private val cacheService: CacheService
) : MemberService {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val cacheTtl = Duration.ofHours(1)

    override suspend fun register(request: MemberRequest): MemberResult<MemberResponse> =
        runCatching {
            logger.info("Registering username: ${request.username}")
            validateRequest(request)
            memberRepository.findByUsername(request.username)?.let {
                logger.warn("Username ${request.username} already exists")
                throw MemberError.AlreadyExists(request.username)
            }
            val member = Member(username = request.username, password = request.password)
            val saved = memberRepository.save(member)
            logger.info("Saved username: ${saved.username}")

            cacheService.putInCache("member:dto:${saved.id}", MemberResponse(saved.id!!, saved.username), cacheTtl)
            MemberResponse(saved.id!!, saved.username)
        }.onFailure {
            logger.error("회원 등록 실패: ${request.username}, 오류: ${it.message}", it)
        }

    override suspend fun findByUsername(username: String): MemberResult<MemberResponse> =
        runCatching {
            cacheService.getCachedOrCompute(
                key = "member:username:$username",
                ttl = cacheTtl
            ) {
                val member = memberRepository.findByUsername(username)
                    ?: throw MemberError.NotFound(username)
                MemberResponse(member.id!!, member.username)
            }.getOrThrow()
        }.onFailure { logger.error("회원 조회 실패: ${it.message}", it) }

    override suspend fun findAll(): Flow<MemberResponse> = flow {
        memberRepository.findAll()
            .map { MemberResponse(it.id!!, it.username) }
            .onEach { logger.info("전체 회원 조회 완료") }
            .forEach { emit(it) }
    }

    override suspend fun count(): MemberResult<Long> =
        runCatching {
            memberRepository.count()
        }.onFailure { logger.error("회원 수 조회 실패: ${it.message}", it) }

    override suspend fun findMemberEntityByUsername(username: String): Result<Member> =
        runCatching {
            val member = memberRepository.findByUsername(username)
                ?: throw MemberError.NotFound(username)
            member
        }.onFailure { logger.error("회원 엔티티 조회 실패: ${it.message}", it) }

    override suspend fun findMemberEntityById(id: Long): Result<Member> =
        runCatching {
            cacheService.getCachedOrCompute(
                key = "member:entity:$id",
                ttl = cacheTtl
            ) {
                val member = memberRepository.findById(id)
                    .orElseThrow { MemberError.NotFound("ID: $id") }
                MemberDTO(member.id!!, member.username, member.password)
            }.map { dto ->
                Member(id = dto.id, username = dto.username, password = dto.password)
            }.getOrThrow()
        }.onFailure {
            logger.error("회원 엔티티 조회 실패 (ID: $id): ${it.message}", it)
        }


    private fun validateRequest(request: MemberRequest) {
        if (request.username.isBlank()) {
            throw MemberError.InvalidInput("username", "사용자 이름은 비어 있을 수 없습니다")
        }
        if (request.password.length < 6) {
            throw MemberError.InvalidInput("password", "비밀번호는 6자 이상이어야 합니다")
        }
    }
}