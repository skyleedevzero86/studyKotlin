package com.komroonga.member.service

import com.komroonga.domain.member.dto.MemberRequest
import com.komroonga.domain.member.dto.MemberResponse
import com.komroonga.global.error.types.MemberError
import com.komroonga.global.utils.CacheService
import com.komroonga.member.entity.Member
import com.komroonga.member.repository.MemberRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
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

    override suspend fun register(request: MemberRequest): MemberResult<MemberResponse> = runCatching {
        withContext(Dispatchers.IO) {
            logger.info("회원 등록 요청: username=${request.username}")
            validateRequest(request)
            memberRepository.findByUsername(request.username)?.let {
                throw MemberError.AlreadyExists(request.username)
            }
            val member = Member(username = request.username, password = request.password)
            val saved = memberRepository.save(member)
            logger.info("회원 등록 성공: id=${saved.id}")
            cacheService.putInCache("member:${saved.id}", MemberResponse(saved.id!!, saved.username), cacheTtl)
            MemberResponse(saved.id!!, saved.username)
        }
    }.onFailure { logger.error("회원 등록 실패: ${it.message}", it) }

    override suspend fun findByUsername(username: String): MemberResult<MemberResponse> = runCatching {
        cacheService.getCachedOrCompute(
            key = "member:username:$username",
            ttl = cacheTtl
        ) {
            withContext(Dispatchers.IO) {
                logger.info("회원 조회 요청: username=$username")
                val member = memberRepository.findByUsername(username)
                    ?: throw MemberError.NotFound(username)
                MemberResponse(member.id!!, member.username)
            }
        }.getOrThrow()
    }.onFailure { logger.error("회원 조회 실패: ${it.message}", it) }

    override suspend fun findAll(): Flow<MemberResponse> {
        logger.info("전체 회원 조회 요청")
        return memberRepository.findAll()
            .map { MemberResponse(it.id!!, it.username) }
            .also { logger.info("전체 회원 조회 완료") }
    }

    override suspend fun count(): MemberResult<Long> = runCatching {
        withContext(Dispatchers.IO) {
            logger.info("회원 수 조회 요청")
            memberRepository.count()
        }
    }.onFailure { logger.error("회원 수 조회 실패: ${it.message}", it) }

    private fun validateRequest(request: MemberRequest) {
        if (request.username.isBlank()) {
            throw MemberError.InvalidInput("username", "사용자 이름은 비어 있을 수 없습니다")
        }
        if (request.password.length < 6) {
            throw MemberError.InvalidInput("password", "비밀번호는 6자 이상이어야 합니다")
        }
    }
}