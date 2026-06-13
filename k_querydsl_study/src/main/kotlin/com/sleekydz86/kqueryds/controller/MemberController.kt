package com.sleekydz86.kqueryds.controller

import com.sleekydz86.kqueryds.dto.MemberSearchCondition
import com.sleekydz86.kqueryds.dto.MemberTeam2Dto
import com.sleekydz86.kqueryds.repository.MemberJpa2Repository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class MemberController(
    private val memberJpa2Repository: MemberJpa2Repository
) {

    @GetMapping("/v1/members")
    fun searchMemberV1(condition: MemberSearchCondition): List<MemberTeam2Dto> {
        return memberJpa2Repository.search(condition)
    }
}