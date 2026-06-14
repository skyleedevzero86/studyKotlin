package com.sleekydz86.kqueryds.repository

import com.sleekydz86.kqueryds.dto.MemberSearch3Condition
import com.sleekydz86.kqueryds.dto.MemberTeam3Dto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface MemberRepositoryCustom {

    fun search(condition: MemberSearch3Condition): List<MemberTeam3Dto>

    fun searchPageSimple(
        condition: MemberSearch3Condition,
        pageable: Pageable
    ): Page<MemberTeam3Dto>

    fun searchPageComplex(
        condition: MemberSearch3Condition,
        pageable: Pageable
    ): Page<MemberTeam3Dto>
}