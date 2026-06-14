package com.sleekydz86.kqueryds.controller

import com.sleekydz86.kqueryds.dto.MemberSearch3Condition
import com.sleekydz86.kqueryds.dto.MemberTeam3Dto
import com.sleekydz86.kqueryds.repository.Member3Repository
import com.sleekydz86.kqueryds.repository.MemberJpa3Repository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class Member3Controller(
    private val memberJpa3Repository: MemberJpa3Repository,
    private val member3Repository: Member3Repository
) {

    @GetMapping("/v1/members")
    fun searchMemberV1(condition: MemberSearch3Condition): List<MemberTeam3Dto> {
        return memberJpa3Repository.search(condition)
    }

    @GetMapping("/v2/members")
    fun searchMemberV2(
        condition: MemberSearch3Condition,
        pageable: Pageable
    ): Page<MemberTeam3Dto> {
        return member3Repository.searchPageSimple(condition, pageable)
    }

    @GetMapping("/v3/members")
    fun searchMemberV3(
        condition: MemberSearch3Condition,
        pageable: Pageable
    ): Page<MemberTeam3Dto> {
        return member3Repository.searchPageComplex(condition, pageable)
    }
}