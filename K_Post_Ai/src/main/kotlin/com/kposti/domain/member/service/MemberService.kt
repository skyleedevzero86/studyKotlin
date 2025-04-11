package com.kposti.domain.member.service

import com.kposti.domain.member.entity.Member
import com.kposti.domain.member.repository.MemberRepository
import com.kposti.global.exceptions.ServiceException
import com.kposti.standard.search.MemberSearchKeywordTypeV1
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.*

@Service
class MemberService(
    private val authTokenService: AuthTokenService,
    private val memberRepository: MemberRepository
) {

    fun count(): Long = memberRepository.count()

    fun join(username: String, password: String, nickname: String, profileImgUrl: String): Member =
        memberRepository.findByUsername(username).ifPresent { throw ServiceException("409-1", "해당 username은 이미 사용중입니다.") }
            .let {
                Member(
                    username = username,
                    password = password,
                    nickname = nickname,
                    apiKey = UUID.randomUUID().toString(),
                    profileImgUrl = profileImgUrl
                ).let(memberRepository::save)
            }

    fun findByUsername(username: String): Optional<Member> = memberRepository.findByUsername(username)

    fun findById(authorId: Long): Optional<Member> = memberRepository.findById(authorId)

    fun findByApiKey(apiKey: String): Optional<Member> = memberRepository.findByApiKey(apiKey)

    fun genAccessToken(member: Member): String = authTokenService.genAccessToken(member)

    fun genAuthToken(member: Member): String = "${member.apiKey} ${genAccessToken(member)}"

    fun getMemberFromAccessToken(accessToken: String): Member? =
        authTokenService.payload(accessToken)?.let {
            Member(
                id = it["id"] as Long,
                username = it["username"] as String,
                nickname = it["nickname"] as String
            )
        }

    fun findByPaged(
        searchKeywordType: MemberSearchKeywordTypeV1?,
        searchKeyword: String?,
        page: Int,
        pageSize: Int
    ): Page<Member> = memberRepository.findByKw(
        searchKeywordType, searchKeyword,
        PageRequest.of(page - 1, pageSize, Sort.by(Sort.Order.desc("id")))
    )

    fun findByPaged(page: Int, pageSize: Int): Page<Member> = findByPaged(null, null, page, pageSize)

    fun modify(member: Member, nickname: String, profileImgUrl: String) {
        member.nickname = nickname
        member.profileImgUrl = profileImgUrl
    }

    fun modifyOrJoin(username: String, nickname: String, profileImgUrl: String): Member =
        findByUsername(username).map {
            modify(it, nickname, profileImgUrl)
            it
        }.orElseGet { join(username, "", nickname, profileImgUrl) }
}
