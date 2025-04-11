package com.kposti.domain.member.repository

import com.kposti.domain.member.entity.Member
import com.kposti.standard.search.MemberSearchKeywordTypeV1
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface MemberRepositoryCustom {
    fun findByKw(kwType: MemberSearchKeywordTypeV1?, kw: String?, pageable: Pageable): Page<Member>
}